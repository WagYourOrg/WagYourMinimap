package xyz.wagyourtail.minimap.map;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.map.chunkdata.cache.AbstractCacher;

import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MapLevel implements AutoCloseable {
    private final LoadingCache<ChunkLocation, ChunkData> chunkCache;
    public final WeakHashMap<ChunkLocation, ChunkData> inMemoryStillCache = new WeakHashMap<>(1024);
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
        chunkCache = builder.build(new CacheLoader<>() {
            @Override
            public ChunkData load(ChunkLocation key) {
                ChunkData data = inMemoryStillCache.get(key);
                if (data != null) {
                    return data;
                }
                for (AbstractCacher cacher : MinimapApi.getInstance().getCachers()) {
                    data = cacher.loadChunk(key);
                    if (data != null) {
                        inMemoryStillCache.put(key, data);
                        return data;
                    }
                }
                data = new ChunkData(key);
                inMemoryStillCache.put(key, data);
                return data;
            }
        });
    }


    @Override
    public synchronized void close() {
        closed = true;
        chunkCache.invalidateAll();
        chunkCache.cleanUp();
    }

    public synchronized ChunkData getChunk(ChunkLocation location) {
        try {
            return chunkCache.get(location);
        } catch (ExecutionException ignored) {}
        return null;
    }

}
