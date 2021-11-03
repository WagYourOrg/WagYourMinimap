package xyz.wagyourtail.minimap.map.chunkdata.cache;

import xyz.wagyourtail.minimap.map.MapServer;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.waypoint.Waypoint;

import java.util.List;
import java.util.stream.Stream;

/**
 * load chunk data from world save data directly in single player as a "backup" to zipcache.
 * this can be slower as it's a fallback... it therefore is necessary to cache the available chunks from each region...
 */
public class FromAnvilCacher extends AbstractCacher{

    public FromAnvilCacher() {
        super(false, true);
    }

    //TODO: implement

    @Override
    public ChunkData loadChunk(ChunkLocation location) {
        return null;
    }

    @Override
    public void saveChunk(ChunkLocation location, ChunkData data) {
        // leave blank
    }

    @Override
    public void saveWaypoints(MapServer server, Stream<Waypoint> waypointList) {
        // leave blank
    }

    @Override
    public List<Waypoint> loadWaypoints(MapServer server) {
        return null;
    }

    @Override
    public void close() {

    }

}
