package xyz.wagyourtail.minimap.chunkdata.cache;

import net.minecraft.client.Minecraft;
import xyz.wagyourtail.minimap.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.map.MapServer;
import xyz.wagyourtail.minimap.waypoint.Waypoint;

import java.util.List;
import java.util.stream.Stream;

public class ServerCacher extends AbstractCacher {

    protected static final Minecraft minecraft = Minecraft.getInstance();

    public ServerCacher() {
        super(SaveOnLoad.NEVER, true);
    }

    //todo: cache chunk hit/fails from server, also make sure to ratelimit these requests
    //todo: finish this

    @Override
    public ChunkData loadChunk(ChunkLocation location) {
        // check if integrated server and cancel
        if (minecraft.isLocalServer()) return null;
        // send loadChunk request packet



        //todo: finish
        return null;
    }

    @Override
    public void saveChunk(ChunkLocation location, ChunkData data) {
        //nope
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
