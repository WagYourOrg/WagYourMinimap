package xyz.wagyourtail.minimap.api;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import xyz.wagyourtail.minimap.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.chunkdata.parts.DataPart;
import xyz.wagyourtail.minimap.chunkdata.updater.AbstractChunkUpdateStrategy;
import xyz.wagyourtail.minimap.waypoint.Waypoint;

public class MinimapEvents {
    public static final Event<ChunkUpdated> CHUNK_UPDATED = EventFactory.createLoop();
    public static final Event<WaypointAdded> WAYPOINT_ADDED = EventFactory.createLoop();
    public static final Event<WaypointRemoved> WAYPOINT_REMOVED = EventFactory.createLoop();
    public static final Event<WaypointUpdated> WAYPOINT_UPDATED = EventFactory.createLoop();

    public interface ChunkUpdated {
        void onChunkUpdate(ChunkLocation location, ChunkData chunkData, Class<? extends AbstractChunkUpdateStrategy> strategy, Class<? extends DataPart<?>> dataPart);

    }

    public interface WaypointAdded {
        void onWaypoint(Waypoint waypoint);

    }

    public interface WaypointRemoved {
        void onWaypoint(Waypoint waypoint);

    }

    public interface WaypointUpdated {
        void onWaypoint(Waypoint old_waypoint, Waypoint new_waypoint);

    }

}
