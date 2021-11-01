package xyz.wagyourtail.minimap.map.chunkdata.cache;

import xyz.wagyourtail.minimap.map.MapServer;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.waypoint.Waypoint;

import java.util.List;
import java.util.stream.Stream;

public class InMemoryStillCacher extends AbstractCacher {

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
