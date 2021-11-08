package xyz.wagyourtail.minimap.chunkdata.cache;

import xyz.wagyourtail.minimap.map.MapServer;
import xyz.wagyourtail.minimap.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.waypoint.Waypoint;

import java.util.List;
import java.util.stream.Stream;

public abstract class AbstractCacher {
    public final SaveOnLoad saveOnLoad;
    public final boolean countHitAsLoad;

    public AbstractCacher(SaveOnLoad saveOnLoad, boolean countHitAsLoad) {
        this.saveOnLoad = saveOnLoad;
        this.countHitAsLoad = countHitAsLoad;
    }

    public abstract ChunkData loadChunk(ChunkLocation location);

    public abstract void saveChunk(ChunkLocation location, ChunkData data);

    public abstract void saveWaypoints(MapServer server, Stream<Waypoint> waypointList);

    public abstract List<Waypoint> loadWaypoints(MapServer server);

    public abstract void close();

    public enum SaveOnLoad {
        NEVER,
        IF_ABOVE,
        IF_BELOW,
        ALWAYS
    }

}
