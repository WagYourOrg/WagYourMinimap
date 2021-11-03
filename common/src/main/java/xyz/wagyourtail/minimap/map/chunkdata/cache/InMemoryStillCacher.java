package xyz.wagyourtail.minimap.map.chunkdata.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.map.MapServer;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.waypoint.Waypoint;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class InMemoryStillCacher extends AbstractCacher {
    private final LoadingCache<ChunkLocation, ChunkData> chunkCache;
    public final Map<ChunkLocation, ChunkData> inMemoryStillCache = new WeakHashMap<>(1024);


    public InMemoryStillCacher() {
        super(true, false);
        chunkCache = CacheBuilder.newBuilder().expireAfterAccess(60000, TimeUnit.MILLISECONDS).build(new CacheLoader<>() {
            @Override
            public ChunkData load(ChunkLocation key) {
                ChunkData data = inMemoryStillCache.get(key);
                if (data != null) {
                    return data;
                }
                inMemoryStillCache.put(key, data);
                return data;
            }
        });
    }

    @Override
    public ChunkData loadChunk(ChunkLocation location) {
        return null;
    }

    @Override
    public void saveChunk(ChunkLocation location, ChunkData data) {

    }

    @Override
    public void saveWaypoints(MapServer server, Stream<Waypoint> waypointList) {

    }

    @Override
    public List<Waypoint> loadWaypoints(MapServer server) {
        return null;
    }

    @Override
    public void close() {

    }

}
