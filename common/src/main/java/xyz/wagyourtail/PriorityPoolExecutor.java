package xyz.wagyourtail;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.concurrent.Executor;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class PriorityPoolExecutor implements Executor, AutoCloseable {

    private static final AtomicInteger poolcount = new AtomicInteger(0);
    private final Thread[] threads;

    private PriorityBlockingQueue<QueueItem> queue = new PriorityBlockingQueue<>(100, Comparator.comparingInt(a -> a.priority));

    private final int defaultPriority;


    public PriorityPoolExecutor() {
        this(Runtime.getRuntime().availableProcessors());
    }

    public PriorityPoolExecutor(int threadCount) {
        this(threadCount, 0);
    }

    public PriorityPoolExecutor(int threadCount, int defaultPriority) {
        this(threadCount, defaultPriority, "PriorityPoolExecutor" + poolcount.getAndIncrement());
    }

    public PriorityPoolExecutor(int threadCount, int defaultPriority, String poolname) {
        this.defaultPriority = defaultPriority;
        threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; ++i) {
            threads[i] = new Thread(this::threadRunner, poolname + "-" + i);
            threads[i].start();
        }
    }

    @Override
    public void execute(@NotNull Runnable command) {
        queue.put(new QueueItem(command, defaultPriority));
    }

    public void execute(@NotNull Runnable command, int priority) {
        queue.put(new QueueItem(command, priority));
    }

    private void threadRunner() {
        try {
            while (true) {
                queue.take().command.run();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        for (Thread thread : threads) {
            thread.interrupt();
        }
        queue = null;
    }

    private static record QueueItem(Runnable command, int priority) {}

}
