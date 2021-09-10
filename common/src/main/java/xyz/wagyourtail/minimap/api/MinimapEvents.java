package xyz.wagyourtail.minimap.api;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import xyz.wagyourtail.ResolveQueue;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.map.chunkdata.updater.AbstractChunkUpdateStrategy;

public class MinimapEvents {
    public static final Event<ChunkUpdateQueued> CHUNK_UPDATE_QUEUED = EventFactory.createLoop();
    public static final Event<ChunkUpdated> CHUNK_UPDATED = EventFactory.createLoop();

    public interface ChunkUpdated {
        void onChunkUpdate(ChunkLocation location, ChunkData chunkData, Class<? extends AbstractChunkUpdateStrategy> strategy);

    }

    public interface ChunkUpdateQueued {
        void onChunkUpdateQueued(ChunkLocation location, ResolveQueue<ChunkData> chunkData, Class<? extends AbstractChunkUpdateStrategy> strategy);

    }

}
