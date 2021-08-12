package xyz.wagyourtail;

import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class LazyResolver<U> {
    private static final ThreadPoolExecutor pool = new ThreadPoolExecutor(1, Runtime.getRuntime().availableProcessors(), 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
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
