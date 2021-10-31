package xyz.wagyourtail.minimap.map.chunkdata.updater;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.api.MinimapEvents;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkLocation;

import java.util.function.BiFunction;

public abstract class AbstractChunkUpdateStrategy {
    public AbstractChunkUpdateStrategy() {
        registerEventListener();
    }

    protected abstract void registerEventListener();

    protected static LayerLightEventListener getBlockLightLayer(Level level) {
        return level.getLightEngine().getLayerListener(LightLayer.BLOCK);
    }

    protected void updateChunk(ChunkLocation location, BiFunction<ChunkLocation, ChunkData, ChunkData> newChunkDataCreator) {
        synchronized (location.level()) {
            ChunkData chunkData = location.level().getChunk(location);
            ChunkData data = newChunkDataCreator.apply(location, chunkData);
            MinimapEvents.CHUNK_UPDATED.invoker().onChunkUpdate(location, data, this.getClass());
            if (chunkData != data) {
                location.level().putChunk(location, data);
            }
        }
    }

    protected ChunkLocation getChunkLocation(Level level, int chunkX, int chunkZ) {
        return ChunkLocation.locationForChunkPos(MinimapApi.getInstance().getMapLevel(level), chunkX, chunkZ);
    }

    protected ChunkLocation getChunkLocation(Level level, ChunkPos pos) {
        return ChunkLocation.locationForChunkPos(MinimapApi.getInstance().getMapLevel(level), pos);
    }

}
