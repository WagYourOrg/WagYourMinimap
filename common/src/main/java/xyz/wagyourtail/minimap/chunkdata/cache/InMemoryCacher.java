package xyz.wagyourtail.minimap.chunkdata.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import xyz.wagyourtail.minimap.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.map.MapServer;
import xyz.wagyourtail.minimap.waypoint.Waypoint;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class InMemoryCacher extends AbstractCacher {
    private final LoadingCache<ChunkLocation, ChunkData> chunkCache;
    public final Map<ChunkLocation, ChunkData> inMemoryStillCache = new WeakHashMap<>(1024);


    public InMemoryCacher() {
        super(SaveOnLoad.ALWAYS, false);
        chunkCache = CacheBuilder.newBuilder().expireAfterAccess(60, TimeUnit.SECONDS).<ChunkLocation, ChunkData>removalListener((e) -> {
            try {
                e.getValue().closeDerivatives();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).build(new CacheLoader<>() {
            @Override
            public ChunkData load(ChunkLocation key) {
                ChunkData data = inMemoryStillCache.get(key);
                if (data != null) {
                    return data;
                }
                throw new RuntimeException("Chunk not found in cache");
            }
        });
    }

    @Override
    public ChunkData loadChunk(ChunkLocation location) {
        try {
            return chunkCache.get(location);
        } catch (Exception ignored) {}
        return null;
    }

    @Override
    public void saveChunk(ChunkLocation location, ChunkData data) {
        ChunkData cd = inMemoryStillCache.get(location);
        if (data != cd) {
            chunkCache.put(location, data);
            inMemoryStillCache.put(location, data);
        }
    }

    @Override
    public void saveWaypoints(MapServer server, Stream<Waypoint> waypointList) {
        // leave empty unless we refactor and remove from waypoint manager or something...
    }

    @Override
    public List<Waypoint> loadWaypoints(MapServer server) {
        // leave empty unless we refactor and remove from waypoint manager or something...
        return null;
    }

    @Override
    public void close() {
        // so umm...
    }

}
