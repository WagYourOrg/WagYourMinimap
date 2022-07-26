package xyz.wagyourtail.minimap.chunkdata.updater;

import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.chunkdata.parts.LightDataPart;
import xyz.wagyourtail.minimap.chunkdata.parts.SurfaceDataPart;
import xyz.wagyourtail.minimap.map.MapServer;
import xyz.wagyourtail.minimap.map.image.SurfaceBlockLightImageStrategy;

import java.util.Set;

public class LightDataUpdater extends AbstractChunkDataUpdater<LightDataPart> {

    public LightDataUpdater() {
        super(Set.of(
            SurfaceBlockLightImageStrategy.class.getCanonicalName()
        ));
    }

    @Override
    public Class<LightDataPart> getType() {
        return LightDataPart.class;
    }

    @Override
    public void onLightLevel(ChunkSource chunkGetter, SectionPos pos) {
        MapServer.MapLevel mapLevel = MinimapApi.getInstance().getMapServer().getLevelFor((Level) chunkGetter.getLevel());
        ChunkAccess chunk = chunkGetter.getChunk(pos.x(), pos.z(), ChunkStatus.FULL, true);
        if (chunk == null) return;
        updateChunk(
            getChunkLocation(mapLevel, pos.x(), pos.z()),
            (location, parent, oldData) -> updateLighting(parent, oldData, chunk, mapLevel, (Level) chunkGetter.getLevel())
        );
    }

//    @Override
//    public void onLoadChunk(ChunkAccess chunk, Level level) {
//        MapServer.MapLevel mapLevel = MinimapApi.getInstance().getMapServer().getLevelFor(level);
//        ChunkPos pos = chunk.getPos();
//        updateNeighborLighting(mapLevel, level, pos.x, pos.z);
//    }
//
//    public void updateNeighborLighting(MapServer.MapLevel level, Level mclevel, int chunkX, int chunkZ) {
//        for (int i = chunkX - 1; i < chunkX + 2; ++i) {
//            for (int j = chunkZ - 1; j < chunkZ + 2; ++j) {
//                if (mclevel.hasChunk(i, j)) {
//                    ChunkAccess chunk = mclevel.getChunk(i, j, ChunkStatus.FULL, false);
//                    if (chunk == null) {
//                        continue;
//                    }
//                    updateChunk(
//                        getChunkLocation(level, i, j),
//                        (location, parent, oldData) -> updateLighting(parent, oldData, chunk, level, mclevel)
//                    );
//                }
//            }
//        }
//    }

    public LightDataPart updateLighting(ChunkData parent, LightDataPart data, ChunkAccess chunk, MapServer.MapLevel level, Level mclevel) {
        if (data == null) {
            data = new LightDataPart(parent);
        }
        SurfaceDataPart surface = parent.getData(SurfaceDataPart.class).orElse(null);
        if (surface == null) {
            return data;
        }
        data.parent.updateTime = System.currentTimeMillis();
        ChunkPos pos = chunk.getPos();
        //TODO: replace with chunk section stuff to not use a MutableBlockPos at all (see baritone), maybe not possible since we need light levels too
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        LayerLightEventListener light = getBlockLightLayer(mclevel);
        for (int i = 0; i < 256; ++i) {
            int x = (i >> 4) % 16;
            int z = i % 16;
            data.blocklight[i] = (byte) light.getLightValue(blockPos.set(
                (pos.x << 4) + x,
                surface.heightmap[i] + 1,
                (pos.z << 4) + z
            ));
        }
        parent.markDirty();
        parent.invalidateDerivitives(Set.of(SurfaceBlockLightImageStrategy.class.getCanonicalName()));
        return data;
    }

}
