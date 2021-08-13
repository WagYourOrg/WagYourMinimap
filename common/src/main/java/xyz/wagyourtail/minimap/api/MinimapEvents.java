package xyz.wagyourtail.minimap.api;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import xyz.wagyourtail.LazyResolver;
import xyz.wagyourtail.minimap.scanner.ChunkData;
import xyz.wagyourtail.minimap.scanner.MapRegion;
import xyz.wagyourtail.minimap.scanner.updater.AbstractChunkUpdateStrategy;

public class MinimapEvents {
    public static final Event<RegionLoaded> REGION_LOADED = EventFactory.createLoop();
    public static final Event<ChunkUpdated> CHUNK_UPDATED = EventFactory.createLoop();

    public interface RegionLoaded {
        void onChunkLoaded(MapRegion region);
    }

    public interface ChunkUpdated {
        void onChunkUpdated(MapRegion region, int chunkIndex, LazyResolver<ChunkData> chunkData, LazyResolver<ChunkData> oldData, Class<? extends AbstractChunkUpdateStrategy> strategy);
    }

}
