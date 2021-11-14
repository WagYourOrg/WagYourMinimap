package xyz.wagyourtail.minimap.chunkdata.updater;

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

public abstract class AbstractChunkUpdateStrategy<T extends DataPart<T>> {
    protected static final Minecraft mc = Minecraft.getInstance();

    public AbstractChunkUpdateStrategy() {
        registerEventListener();
    }

    protected abstract void registerEventListener();

    protected static LayerLightEventListener getBlockLightLayer(Level level) {
        return level.getLightEngine().getLayerListener(LightLayer.BLOCK);
    }

    protected void updateChunk(ChunkLocation location, ChunkUpdateListener<T> newChunkDataCreator) {
        synchronized (location.level()) {
            ChunkData chunkData = location.get();
            chunkData.computeData(getType(), (old) -> newChunkDataCreator.onChunkUpdate(location, chunkData, old));
            MinimapEvents.CHUNK_UPDATED.invoker().onChunkUpdate(location, chunkData, this.getClass(), getType());
        }
    }

    public abstract Class<T> getType();

    protected ChunkLocation getChunkLocation(MapServer.MapLevel level, int chunkX, int chunkZ) {
        return ChunkLocation.locationForChunkPos(level, chunkX, chunkZ);
    }

    protected ChunkLocation getChunkLocation(MapServer.MapLevel level, ChunkPos pos) {
        return ChunkLocation.locationForChunkPos(level, pos);
    }

    public interface ChunkUpdateListener<T extends DataPart<T>> {
        T onChunkUpdate(ChunkLocation location, ChunkData data, T oldData);

    }

}
