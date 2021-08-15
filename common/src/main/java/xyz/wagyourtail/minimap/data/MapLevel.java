package xyz.wagyourtail.minimap.data;

import com.google.common.cache.*;
import xyz.wagyourtail.LazyResolver;
import xyz.wagyourtail.minimap.WagYourMinimap;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.data.cache.AbstractCacher;
import xyz.wagyourtail.minimap.data.cache.ZipCacher;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MapLevel extends CacheLoader<ChunkLocation, LazyResolver<ChunkData>> implements AutoCloseable {
    public final String server_slug;
    public final String level_slug;
    private LoadingCache<ChunkLocation, LazyResolver<ChunkData>> regionCache;
    private static AbstractCacher[] cachers = new AbstractCacher[] {new ZipCacher()};
    public final int minHeight, maxHeight;
    private boolean closed = false;

    public MapLevel(String server_slug, String level_slug, int minHeight, int maxHeight) {
        this.server_slug = server_slug;
        this.level_slug = level_slug;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
        resizeCache(MinimapApi.getInstance().getConfig().regionCacheSize);
    }

    public void onRegionRemoved(RemovalNotification<ChunkLocation, LazyResolver<ChunkData>> notification) {
        WagYourMinimap.LOGGER.debug("expiring region {} from map cache", notification.getKey());
        try {
            if (notification.getCause() == RemovalCause.REPLACED) return;
            new LazyResolver<>(() -> {
                ChunkData data = notification.getValue().resolve();
                if (data != null) {
                    for (AbstractCacher cacher : cachers) {
                        cacher.save(notification.getKey(), data);
                    }
                }
                notification.getValue().close();
                return null;
            }).resolveAsync(0);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    //wtf, why doesn't newBuilder do this method sig instead of just <Object, Object>
    private <K,V> CacheBuilder<K, V> createCache() {
        return (CacheBuilder) CacheBuilder.newBuilder();
    }


    public synchronized void resizeCache(long newCacheSize) {
        CacheBuilder<ChunkLocation, LazyResolver<ChunkData>> builder = createCache()
            .maximumSize(newCacheSize)
            .expireAfterAccess(60000, TimeUnit.MILLISECONDS)
            .removalListener(this::onRegionRemoved);

        LoadingCache<ChunkLocation, LazyResolver<ChunkData>> oldCache = regionCache;
        regionCache = builder.build(this);
        if (oldCache != null) {
            regionCache.putAll(oldCache.asMap());
        }
    }

    @Override
    public synchronized void close() {
        closed = true;
        regionCache.invalidateAll();
        regionCache.cleanUp();
    }

    public synchronized void setChunk(ChunkLocation location, LazyResolver<ChunkData> newData) {
        regionCache.put(location, newData);
        if (closed) {
            regionCache.invalidateAll();
            regionCache.cleanUp();
        }
    }

    public synchronized LazyResolver<ChunkData> getChunk(ChunkLocation location) {
        try {
            return regionCache.get(location);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return new LazyResolver<>((ChunkData) null);
    }

    @Override
    public LazyResolver<ChunkData> load(ChunkLocation key) throws Exception {
        return new LazyResolver<>(() -> {
            for (AbstractCacher cacher : cachers) {
                ChunkData data = cacher.load(key);
                if (data != null) return data;
            }
            return null;
        });
    }
}
