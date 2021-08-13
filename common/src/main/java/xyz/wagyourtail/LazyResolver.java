package xyz.wagyourtail;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

public class LazyResolver<U> {
    private static final int availableThreads = Math.min(Math.max(1, Runtime.getRuntime().availableProcessors() - 1), 4);
    private static final ThreadPoolExecutor pool = new ThreadPoolExecutor(availableThreads, availableThreads, 0L, TimeUnit.NANOSECONDS, new LinkedBlockingQueue<>(), new ThreadFactory() {
        final AtomicInteger threadCount = new AtomicInteger(1);
        @Override
        public Thread newThread(@NotNull Runnable r) {
            return new Thread(r, "LazyResolverPool-" + threadCount.getAndIncrement());
        }
    });
    private final Supplier<U> supplier;
    private final AtomicBoolean pooled = new AtomicBoolean(false);
    private boolean done = false;
    private U result = null;

    public LazyResolver(@NotNull Supplier<U> supplier) {
        this.supplier = supplier;
    }

    public LazyResolver(Supplier<U> supplier, LazyResolver<U> previous) {
        this.supplier = supplier;
        if (previous.done) result = previous.result;
    }

    public LazyResolver(U resolved) {
        supplier = null;
        done = true;
        result = resolved;
    }

    public U resolve() {
        //optimistically check if already done
        if (done) return result;
        synchronized (this) {
            //check if done in synchronized
            if (done) return result;
            U oldResult = result;
            result = supplier.get();
            //close defaulted value
            if (oldResult != null) {
                if (oldResult instanceof AutoCloseable close) {
                    try {
                        close.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            //after result to make optimistic not break
            done = true;
        }
        return result;
    }

    public U resolveAsync(long maxWaitTimeMS) throws ExecutionException, InterruptedException, TimeoutException {
        // optimistically check if already done
        if (this.done) return result;
        // check if already pooled and shortcut to returning whatever result is if not waiting for done.
        synchronized (this.pooled) {
            if (this.pooled.get() && maxWaitTimeMS == 0) return result;
        }
        // sync on pool so we don't accidentally pool this twice...
        synchronized (this.pooled) {
            if (!this.pooled.get()) {
                pool.execute(() -> {
                    resolve();
                    synchronized (supplier) {
                        supplier.notifyAll();
                    }
                });
                this.pooled.set(true);
            }
        }
        // don't synchronize on "this" so it doesn't jam when resolve() is running. so since I don't want to make any extra object fields, this was the only one left we weren't using
        if (maxWaitTimeMS > 0) {
            synchronized (supplier) {
                supplier.wait(maxWaitTimeMS);
            }
        }
        return result;
    }

    public U orElse(U value) {
        if (done) return result;
        synchronized (this) {
            if (done) return result;
        }
        return value;
    }

    public synchronized void close() {
        if (done && result instanceof AutoCloseable) {
            try {
                ((AutoCloseable) result).close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        done = true;
        result = null;
    }

    public synchronized boolean isDone() {
        return done;
    }

    public <V> LazyResolver<V> then(Function<U, V> then) {
        return new LazyResolver<>(() -> then.apply(resolve()));
    }

    public LazyResolver<U> then(Function<U, U> then, boolean previous) {
        return previous ? new LazyResolver<>(() -> then.apply(resolve()), this) : new LazyResolver<>(() -> then.apply(resolve()));
    }

}
