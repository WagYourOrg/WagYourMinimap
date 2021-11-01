package xyz.wagyourtail.minimap.map;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkLocation;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MapLevel extends CacheLoader<ChunkLocation, ChunkData> implements AutoCloseable {
    private final LoadingCache<ChunkLocation, ChunkData> regionCache;
    public final MapServer parent;
    public final String level_slug;
    public final int minHeight, maxHeight;
    private boolean closed = false;

    public MapLevel(MapServer parent, String level_slug, int minHeight, int maxHeight) {
        this.parent = parent;
        this.level_slug = level_slug;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
        CacheBuilder<ChunkLocation, ChunkData> builder = CacheBuilder.newBuilder()
            .expireAfterAccess(60000, TimeUnit.MILLISECONDS).removalListener(e -> {
                try {
                    e.getValue().close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        regionCache = builder.build(this);
    }


    @Override
    public synchronized void close() {
        closed = true;
        regionCache.invalidateAll();
        regionCache.cleanUp();
    }

    public synchronized ChunkData getChunk(ChunkLocation location) {
        try {
            return regionCache.get(location);
        } catch (ExecutionException ignored) {}
        return null;
    }

    public synchronized void putChunk(ChunkLocation location, ChunkData data) {
        if (closed) {
            return;
        }
        regionCache.put(location, data);
    }

    @Override
    public ChunkData load(ChunkLocation key) throws Exception {
        return MapServer.loadChunk(key);
    }

}
