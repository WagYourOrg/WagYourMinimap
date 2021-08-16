package xyz.wagyourtail.minimap.data;

import com.google.common.cache.*;
import xyz.wagyourtail.ResolveQueue;
import xyz.wagyourtail.minimap.data.cache.AbstractCacher;
import xyz.wagyourtail.minimap.data.cache.ZipCacher;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class MapLevel extends CacheLoader<ChunkLocation, ResolveQueue<ChunkData>> implements AutoCloseable {
    public static final ThreadPoolExecutor pool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.NANOSECONDS, new LinkedBlockingQueue<>());
    public final String server_slug;
    public final String level_slug;
    private final LoadingCache<ChunkLocation, ResolveQueue<ChunkData>> regionCache;
    private static AbstractCacher[] cachers = new AbstractCacher[] {new ZipCacher()};
    public final int minHeight, maxHeight;
    private boolean closed = false;

    public MapLevel(String server_slug, String level_slug, int minHeight, int maxHeight) {
        this.server_slug = server_slug;
        this.level_slug = level_slug;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
        CacheBuilder<ChunkLocation, ResolveQueue<ChunkData>> builder = CacheBuilder.newBuilder()
            .expireAfterAccess(60000, TimeUnit.MILLISECONDS).removalListener(e -> {
                try {
                    e.getValue().close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        regionCache = builder.build(this);
    }

    private static final AtomicInteger saving = new AtomicInteger(0);

    public void saveChunk(ChunkLocation location, ChunkData data) {
        saving.incrementAndGet();
        pool.execute(() -> {
            innerRemove(location, data);
            saving.decrementAndGet();
        });
    }

    private void innerRemove(ChunkLocation location, ChunkData data) {
        if (data != null) {
            synchronized (data) {
                for (AbstractCacher cacher : cachers) {
                    cacher.save(location, data);
                }
            }
        }
    }

    public static int getSaving() {
        return saving.get();
    }

    @Override
    public synchronized void close() {
        closed = true;
        regionCache.invalidateAll();
        regionCache.cleanUp();
    }

    public synchronized ResolveQueue<ChunkData> getChunk(ChunkLocation location) {
        try {
            return regionCache.get(location);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return new ResolveQueue<>(null, null);
    }

    @Override
    public ResolveQueue<ChunkData> load(ChunkLocation key) throws Exception {
        return new ResolveQueue<>((a) -> {
            for (AbstractCacher cacher : cachers) {
                ChunkData data = cacher.load(key);
                if (data != null) return data;
            }
            return null;
        });
    }
}
