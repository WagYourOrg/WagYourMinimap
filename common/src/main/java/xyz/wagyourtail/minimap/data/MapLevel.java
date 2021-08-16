package xyz.wagyourtail.minimap.data;

import com.google.common.cache.*;
import xyz.wagyourtail.ResolveQueue;
import xyz.wagyourtail.minimap.data.cache.AbstractCacher;
import xyz.wagyourtail.minimap.data.cache.ZipCacher;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MapLevel extends CacheLoader<ChunkLocation, ResolveQueue<ChunkData>> implements AutoCloseable {
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
        CacheBuilder<ChunkLocation, ResolveQueue<ChunkData>> builder = createCache()
            .expireAfterAccess(60000, TimeUnit.MILLISECONDS)
            .removalListener(this::onRegionRemoved);
        regionCache = builder.build(this);
    }

    public void onRegionRemoved(RemovalNotification<ChunkLocation, ResolveQueue<ChunkData>> notification) {
        CompletableFuture.runAsync(() -> {
            ChunkData data = null;
            try {
                data = notification.getValue().get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (data != null) {
                for (AbstractCacher cacher : cachers) {
                    cacher.save(notification.getKey(), data);
                }
            }
            try {
                notification.getValue().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    //wtf, why doesn't newBuilder do this method sig instead of just <Object, Object>
    private <K,V> CacheBuilder<K, V> createCache() {
        return (CacheBuilder) CacheBuilder.newBuilder();
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
        return new ResolveQueue<>((nullVal) -> {
            for (AbstractCacher cacher : cachers) {
                ChunkData data = cacher.load(key);
                if (data != null) return data;
            }
            return null;
        });
    }
}
