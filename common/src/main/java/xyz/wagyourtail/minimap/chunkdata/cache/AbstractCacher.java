package xyz.wagyourtail.minimap.chunkdata.cache;

import xyz.wagyourtail.minimap.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.map.MapServer;
import xyz.wagyourtail.minimap.waypoint.Waypoint;

import java.util.List;

public abstract class AbstractCacher {
    public abstract ChunkData loadChunk(ChunkLocation location);

    public abstract void saveChunk(ChunkLocation location, ChunkData data);

    public abstract void saveWaypoints(MapServer server, List<Waypoint> waypointList);

    public abstract List<Waypoint> loadWaypoints(MapServer server);

}
