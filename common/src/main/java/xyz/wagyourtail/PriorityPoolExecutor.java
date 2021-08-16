package xyz.wagyourtail;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class PriorityPoolExecutor implements Executor, AutoCloseable {

    private static final AtomicInteger poolcount = new AtomicInteger(0);
    private final Thread[] threads;

    private PriorityBlockingQueue<QueueItem> queue = new PriorityBlockingQueue<>(100, (a,b) -> b.priority-a.priority+1);

    private final int defaultPriority;


    public PriorityPoolExecutor() {
        this(Runtime.getRuntime().availableProcessors());
    }

    public PriorityPoolExecutor(int threadCount) {
        this(threadCount, 0);
    }

    public PriorityPoolExecutor(int threadCount, int defaultPriority) {
        this(threadCount, defaultPriority, "PriorityPoolExecutor" + poolcount.getAndIncrement(), Thread.NORM_PRIORITY);
    }

    public PriorityPoolExecutor(int threadCount, int defaultPriority, String poolname, int threadPriority) {
        this.defaultPriority = defaultPriority * 2;
        threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; ++i) {
            threads[i] = new Thread(this::threadRunner, poolname + "-" + i);
            threads[i].setPriority(threadPriority);
            threads[i].start();
        }
    }

    @Override
    public void execute(@NotNull Runnable command) {
        queue.put(new QueueItem(command, defaultPriority));
    }

    public void execute(@NotNull Runnable command, int priority) {
        queue.put(new QueueItem(command, priority * 2));
    }

    private void threadRunner() {
        try {
            while (true) {
                 queue.take().command.run();
                 Thread.yield();
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
