package xyz.wagyourtail;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

public class LazyResolver<U> {
    private static final int availableThreads = Math.min(Math.max(1, Runtime.getRuntime().availableProcessors() - 1), 4);
    private static final ThreadPoolExecutor pool = new ThreadPoolExecutor(availableThreads, availableThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new ThreadFactory() {
        final AtomicInteger threadCount = new AtomicInteger(1);
        @Override
        public Thread newThread(@NotNull Runnable r) {
            return new Thread(r, "LazyResolverPool-" + threadCount.getAndIncrement());
        }
    });
    private final Supplier<U> supplier;
    private boolean done = false;
    private U result = null;

    public LazyResolver(Supplier<U> supplier) {
        this.supplier = supplier;
    }

    public synchronized U resolve() {
        if (!done) {
            done = true;
            result = supplier.get();
        }
        return result;
    }

    public U resolveAsync(long maxWaitTimeMS) throws ExecutionException, InterruptedException, TimeoutException {
        synchronized (this) {
            if (this.done) return result;
        }
        Object o = new Object();
        pool.execute(() -> {
            synchronized (this) {
                resolve();
                synchronized (o) {
                    o.notify();
                }
            }
        });
        synchronized (o) {
            o.wait(maxWaitTimeMS);
        }
        return result;
    }

    public synchronized boolean isDone() {
        return done;
    }

    public <V> LazyResolver<V> then(Function<U, V> then) {
        return new LazyResolver<>(() -> then.apply(resolve()));
    }

}
