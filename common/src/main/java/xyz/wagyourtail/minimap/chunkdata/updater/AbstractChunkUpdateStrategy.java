package xyz.wagyourtail.minimap.chunkdata.updater;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import xyz.wagyourtail.ResolveQueue;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.api.MinimapEvents;
import xyz.wagyourtail.minimap.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.chunkdata.ChunkLocation;

import java.util.function.BiFunction;

public abstract class AbstractChunkUpdateStrategy {
    private final int priority;

    public AbstractChunkUpdateStrategy(int priority) {
        registerEventListener();
        this.priority = priority;
    }

    protected abstract void registerEventListener();

    protected static LayerLightEventListener getBlockLightLayer(Level level) {
        return level.getLightEngine().getLayerListener(LightLayer.BLOCK);
    }

    protected void updateChunk(ChunkLocation location, BiFunction<ChunkLocation, ChunkData, ChunkData> newChunkDataCreator) {
        synchronized (location.level()) {
            ResolveQueue<ChunkData> chunkData = location.level().getChunk(location);
            chunkData.addTask((od) -> newChunkDataCreator.apply(location, od), priority);
            MinimapEvents.CHUNK_UPDATED.invoker().onChunkUpdated(location, chunkData, this.getClass());
        }
    }

    protected ChunkLocation getChunkLocation(Level level, int chunkX, int chunkZ) {
        return ChunkLocation.locationForChunkPos(MinimapApi.getInstance().getMapLevel(level), chunkX, chunkZ);
    }

    protected ChunkLocation getChunkLocation(Level level, ChunkPos pos) {
        return ChunkLocation.locationForChunkPos(MinimapApi.getInstance().getMapLevel(level), pos);
    }

}
