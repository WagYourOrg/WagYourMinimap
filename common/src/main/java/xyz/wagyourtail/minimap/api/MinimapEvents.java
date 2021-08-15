package xyz.wagyourtail.minimap.api;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import xyz.wagyourtail.LazyResolver;
import xyz.wagyourtail.minimap.data.ChunkData;
import xyz.wagyourtail.minimap.data.ChunkLocation;
import xyz.wagyourtail.minimap.data.updater.AbstractChunkUpdateStrategy;

public class MinimapEvents {
    public static final Event<ChunkUpdated> CHUNK_UPDATED = EventFactory.createLoop();

    public interface ChunkUpdated {
        void onChunkUpdated(ChunkLocation location, LazyResolver<ChunkData> chunkData, LazyResolver<ChunkData> oldData, Class<? extends AbstractChunkUpdateStrategy> strategy);
    }

}
