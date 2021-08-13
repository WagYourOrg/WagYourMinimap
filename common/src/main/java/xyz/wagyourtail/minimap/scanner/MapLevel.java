package xyz.wagyourtail.minimap.scanner;

import com.google.common.cache.*;
import xyz.wagyourtail.LazyResolver;
import xyz.wagyourtail.minimap.WagYourMinimap;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.api.MinimapEvents;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MapLevel implements AutoCloseable {
    public final String server_slug;
    public final String level_slug;
    public final Path mapCacheLocation;
    private LoadingCache<Pos, MapRegion> regionCache;
    public final int minHeight, maxHeight;

    public MapLevel(String server_slug, String level_slug, int minHeight, int maxHeight) {
        this.server_slug = server_slug;
        this.level_slug = level_slug;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
        mapCacheLocation = MinimapClientApi.getInstance().configFolder.resolve(server_slug + "/" + level_slug);
        resizeCache(MinimapApi.getInstance().getConfig().regionCacheSize);
    }

    public void onRegionRemoved(RemovalNotification<Pos, MapRegion> notification) {
        WagYourMinimap.LOGGER.debug("expiring region {} from map cache", notification.getKey());
        try {
            new LazyResolver<>(() -> {
                saveRegion(notification.getKey(), notification.getValue());
                notification.getValue().close();
                return null;
            }).resolveAsync(0);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    public void saveRegion(Pos pos, MapRegion region) {
        try {
            File cacheLoc = mapCacheLocation.toFile();
            if (!cacheLoc.exists() && !cacheLoc.mkdirs()) throw new IOException("Failed to make directory for cache.");
            region.writeRegion(mapCacheLocation.resolve(pos.getString() + ".zip"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //wtf, why doesn't newBuilder do this method sig instead of just <Object, Object>
    private <K,V> CacheBuilder<K, V> createCache() {
        return (CacheBuilder) CacheBuilder.newBuilder();
    }

    public synchronized MapRegion getRegion(Pos pos) throws ExecutionException {
        return regionCache.get(pos);
    }

    public synchronized void resizeCache(long newCacheSize) {
        CacheBuilder<Pos, MapRegion> builder = createCache()
            .maximumSize(newCacheSize)
            .expireAfterAccess(60000, TimeUnit.MILLISECONDS)
            .removalListener(this::onRegionRemoved);

        LoadingCache<Pos, MapRegion> oldCache = regionCache;
        regionCache = builder.build(new RegionLoader());
        if (oldCache != null) {
            regionCache.putAll(oldCache.asMap());
        }
    }

    @Override
    public synchronized void close() {
        regionCache.invalidateAll();
        regionCache.cleanUp();
    }

    public static record Pos(int x, int z) {
        public String getString() {
            return x + "," + z;
        }
    }

    public class RegionLoader extends CacheLoader<Pos, MapRegion> {

        @Override
        public MapRegion load(Pos key) throws IOException {
            MapRegion region = new MapRegion(MapLevel.this, key);
            if (mapCacheLocation.resolve(key.getString() + ".zip").toFile().exists()) {
                region.readRegion(mapCacheLocation.resolve(key.getString() + ".zip"));
            }
            MinimapEvents.REGION_LOADED.invoker().onChunkLoaded(region);
            return region;
        }

    }

}
