package xyz.wagyourtail.minimap.map.image;

import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.world.level.Level;
import xyz.wagyourtail.minimap.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.chunkdata.ChunkLocation;

public class UndergroundVanillaImageStrategy extends VanillaMapImageStrategy {

    private final ThreadLocal<Integer> lastY = ThreadLocal.withInitial(() -> -1);
    private final int resolution;

    public UndergroundVanillaImageStrategy(int resolution) {
        this.resolution = resolution;
    }

    @Override
    public DynamicTexture load(ChunkLocation location, ChunkData data) {
        Level level = minecraft.level;
        if (level == null || minecraft.cameraEntity == null) {
            return null;
        }
        int y = minecraft.cameraEntity.getBlockY() - level.dimensionType().minY();
        y = y - y % resolution;
        lastY.set(y);
        //TODO: make this work
        //        if (MapServer.getLevelName(level).equals(location.level().level_slug())) {
        //            ChunkAccess chunk = level.getChunk(location.getChunkX(), location.getChunkZ(), ChunkStatus.FULL, false);
        //            if (chunk != null) {
        //
        //
        //
        //            }
        //        }
        return null;
    }

    @Override
    public String getDerivitiveKey() {
        return super.getDerivitiveKey() + "$" + resolution + "$" + lastY.get().toString();
    }

}
