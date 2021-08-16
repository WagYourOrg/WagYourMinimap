package xyz.wagyourtail;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class ResolveQueue<U> {
    private static final int availableThreads = Math.min(Math.max(1, Runtime.getRuntime().availableProcessors() - 1), 4);
    private static final int defaultPriority = 10;
    private static final PriorityPoolExecutor pool = new PriorityPoolExecutor(availableThreads, defaultPriority, "ResolveQueuePool");
    private U current = null;
    private final AtomicBoolean runningNext = new AtomicBoolean(false);
    private final Queue<QueueItem<U>> queue = new LinkedList<>();
    private boolean closed = false;

    public ResolveQueue(@NotNull  Function<U, U> firstResolve) {
        queue.add(new QueueItem<>(firstResolve, defaultPriority));

    }

    public ResolveQueue(Function<U, U> next, U initial) {
        if (next != null) queue.add(new QueueItem<>(next, defaultPriority));
        current = initial;
    }


    public synchronized ResolveQueue<U> addTask(@NotNull Function<U, U> next) {
        addTask(next, defaultPriority);
        return this;
    }

    public synchronized ResolveQueue<U> addTask(@NotNull Function<U, U> next, int poolPriority) {
        queue.add(new QueueItem<>(next, poolPriority));
        return this;
    }

    public synchronized U getNowNoResolve() {
        if (closed) return null;
        return current;
    }

    public synchronized U getNow() {
        if (closed) return null;
        runNextAsync();
        return current;
    }

    public U get() throws InterruptedException {
        synchronized (this) {
            if (closed) return null;
        }
        int size = queue.size();
        for (int i = 0; i < size; ++i) {
            runNextNow();
        }
        return current;
    }

    private void runNextNow() throws InterruptedException {
        QueueItem<U> next;
        synchronized (this) {
            if (runningNext.get()) {
                this.wait();
            }
            next = queue.poll();
            if (next == null) return;
            runningNext.set(true);
        }
        runNextInner(next);
    }

    private synchronized void runNextAsync() {
        if (!runningNext.get() && !queue.isEmpty()) {
            QueueItem<U> next = queue.poll();
            assert next != null;
            runningNext.set(true);
            pool.execute(() ->
                runNextInner(next)
            , next.priority);
        }
    }

    private void runNextInner(QueueItem<U> next) {
        U newVal = next.next.apply(current);
        synchronized (this) {
            U oldVal = current;
            current = newVal;
            if (oldVal != newVal) {
                if (oldVal instanceof AutoCloseable old) {
                    try {
                        old.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            this.notifyAll();
            runningNext.set(false);
        }
    }

    public synchronized void close() throws Exception {
        closed = true;
        if (runningNext.get()) {
            this.wait();
        }
        if (current instanceof AutoCloseable c) {
            ((AutoCloseable) current).close();
        }
    }

    private static record QueueItem<U>(Function<U, U> next, int priority) {}
}
