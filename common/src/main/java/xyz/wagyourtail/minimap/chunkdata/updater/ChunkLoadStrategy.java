package xyz.wagyourtail.minimap.chunkdata.updater;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.chunkdata.parts.SurfaceDataPart;
import xyz.wagyourtail.minimap.map.MapServer;

public class ChunkLoadStrategy extends AbstractChunkUpdateStrategy<SurfaceDataPart> {

    public static final Event<Load> LOAD = EventFactory.createLoop();

    public ChunkLoadStrategy() {
        super();
    }

    @Override
    protected void registerEventListener() {
        LOAD.register((chunk, level) -> {
            if (level != mc.level) {
                return;
            }
            MapServer.MapLevel mapLevel = MinimapApi.getInstance().getMapServer().getCurrentLevel();
            ChunkPos pos = chunk.getPos();
            updateChunk(getChunkLocation(mapLevel, pos),
                (location, parent, oldData) -> loadFromChunk(chunk, mapLevel, level, parent, oldData)
            );
        });
    }

    @Override
    public Class<SurfaceDataPart> getType() {
        return SurfaceDataPart.class;
    }

    public static SurfaceDataPart loadFromChunk(ChunkAccess chunk, MapServer.MapLevel level, Level mclevel, ChunkData parent, SurfaceDataPart oldSurfaceData) {
        SurfaceDataPart data = new SurfaceDataPart(parent);
        data.parent.updateTime = System.currentTimeMillis();
        ChunkPos pos = chunk.getPos();
        //TODO: replace with chunk section stuff to not use a MutableBlockPos at all (see baritone), maybe not possible since we need light levels too
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        Registry<Biome> biomeRegistry = mclevel.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
        LayerLightEventListener light = getBlockLightLayer(mclevel);
        if (mclevel.dimensionType().hasCeiling()) {
            int ceiling = mclevel.dimensionType().logicalHeight() - 1;
            for (int i = 0; i < 256; ++i) {
                int x = (i >> 4) % 16;
                int z = i % 16;
                blockPos.set((pos.x << 4) + x, 0, (pos.z << 4) + z);
                boolean air = false;
                for (int j = ceiling; j > mclevel.getMinBuildHeight(); --j) {
                    BlockState block = chunk.getBlockState(blockPos.setY(j));
                    if (block.getBlock() instanceof AirBlock) {
                        air = true;
                    } else if (air) {
                        data.heightmap[i] = j;
                        data.blockid[i] = parent.getOrRegisterBlockState(block);
                        data.biomeid[i] = parent.getOrRegisterBiome(biomeRegistry.getKey(chunk.getBiomes()
                            .getNoiseBiome(x >> 2, data.heightmap[i] >> 2, z >> 2)));
                        //                        data.biomeid[i] = data.getOrRegisterResourceLocation(biomeRegistry.getKey(level.getBiome(blockPos)));
                        data.blocklight[i] = (byte) light.getLightValue(blockPos.setY(data.heightmap[i] + 1));
                        break;
                    }
                }
            }
        } else {
            for (int i = 0; i < 256; ++i) {
                int x = (i >> 4) % 16;
                int z = i % 16;
                data.heightmap[i] = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                BlockState top = chunk.getBlockState(blockPos.set((pos.x << 4) + x,
                    data.heightmap[i],
                    (pos.z << 4) + z
                ));
                data.blockid[i] = parent.getOrRegisterBlockState(top);
                data.biomeid[i] = parent.getOrRegisterBiome(biomeRegistry.getKey(chunk.getBiomes()
                    .getNoiseBiome(x >> 2, data.heightmap[i] >> 2, z >> 2)));
                //                data.biomeid[i] = data.getOrRegisterResourceLocation(biomeRegistry.getKey(level.getBiome(blockPos)));
                data.blocklight[i] = (byte) light.getLightValue(blockPos.setY(data.heightmap[i] + 1));

                if (top.getBlock().equals(Blocks.WATER)) {
                    BlockState b = top;
                    while (b.getBlock().equals(Blocks.WATER)) {
                        b = chunk.getBlockState(blockPos.setY(blockPos.getY() - 1));
                    }
                    data.oceanFloorHeightmap[i] = blockPos.getY();
                    data.oceanFloorBlockid[i] = parent.getOrRegisterBlockState(chunk.getBlockState(blockPos));
                }
            }
        }

        // update south heightmap
        //        UpdateNorthHeightmapStrategy.UPDATE_EVENT.invoker().onUpdate(parent.south(), data.heightmap);
        //        UpdateSouthHeightmapStrategy.UPDATE_EVENT.invoker().onUpdate(parent.north(), data.heightmap);
        parent.north().get().invalidateDerivitives();
        parent.south().get().invalidateDerivitives();

        if (oldSurfaceData != null) {
            oldSurfaceData.mergeFrom(data);
            return oldSurfaceData;
        }
        data.parent.markDirty();
        return data;
    }

    public interface Load {
        void onLoadChunk(ChunkAccess chunk, Level level);

    }

}
