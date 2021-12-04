package xyz.wagyourtail.minimap.chunkdata.updater;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import xyz.wagyourtail.minimap.api.MinimapEvents;
import xyz.wagyourtail.minimap.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.chunkdata.parts.DataPart;
import xyz.wagyourtail.minimap.map.MapServer;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class AbstractChunkDataUpdater<T extends DataPart<T>> implements ChunkLoadEvent, BlockUpdateEvent {
    private static final ThreadPoolExecutor data_pool = new ThreadPoolExecutor(
        1,
        1,
        0L,
        TimeUnit.NANOSECONDS,
        new LinkedBlockingQueue<>()
    );

    protected static final Minecraft mc = Minecraft.getInstance();
    public static final Event<ChunkLoadEvent> CHUNK_LOAD = EventFactory.createLoop();
    public static final Event<BlockUpdateEvent> BLOCK_UPDATE = EventFactory.createLoop();

    public final Set<String> derivitivesToInvalidate = new HashSet<>();

    public AbstractChunkDataUpdater(Set<String> derivitivesToInvalidate) {
        registerEventListener();
        this.derivitivesToInvalidate.addAll(derivitivesToInvalidate);
    }

    protected void registerEventListener() {
        CHUNK_LOAD.register(this);
        BLOCK_UPDATE.register(this);
    }

    protected static LayerLightEventListener getBlockLightLayer(Level level) {
        return level.getLightEngine().getLayerListener(LightLayer.BLOCK);
    }

    protected void updateChunk(ChunkLocation location, ChunkUpdateListener<T> newChunkDataCreator) {
//        synchronized (location.level()) {
            data_pool.execute(() -> {
                ChunkData chunkData = location.get();
                chunkData.computeData(getType(), (old) -> newChunkDataCreator.onChunkUpdate(location, chunkData, old));
                MinimapEvents.CHUNK_UPDATED.invoker().onChunkUpdate(location, chunkData, this.getClass(), getType());
            });
//        }
    }

    public abstract Class<T> getType();

    protected ChunkLocation getChunkLocation(MapServer.MapLevel level, int chunkX, int chunkZ) {
        return ChunkLocation.locationForChunkPos(level, chunkX, chunkZ);
    }

    protected ChunkLocation getChunkLocation(MapServer.MapLevel level, ChunkPos pos) {
        return ChunkLocation.locationForChunkPos(level, pos);
    }

    public static interface ChunkUpdateListener<T extends DataPart<T>> {
        T onChunkUpdate(ChunkLocation location, ChunkData data, T oldData);

    }

}
