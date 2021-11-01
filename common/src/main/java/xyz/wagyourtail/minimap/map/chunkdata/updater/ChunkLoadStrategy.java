package xyz.wagyourtail.minimap.map.chunkdata.updater;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.map.chunkdata.parts.SurfaceDataPart;

public class ChunkLoadStrategy extends AbstractChunkUpdateStrategy<SurfaceDataPart> {

    public static final Event<Load> LOAD = EventFactory.createLoop();

    public ChunkLoadStrategy() {
        super();
    }

    @Override
    protected void registerEventListener() {
        LOAD.register((chunk, level) -> {
            ChunkPos pos = chunk.getPos();
            updateChunk(
                getChunkLocation(level, pos),
                (location, parent, oldData) -> loadFromChunk(location, chunk, level, parent, oldData)
            );
        });
    }

    @Override
    public Class<SurfaceDataPart> getType() {
        return SurfaceDataPart.class;
    }

    public static SurfaceDataPart loadFromChunk(ChunkLocation location, ChunkAccess chunk, Level level, ChunkData parent, SurfaceDataPart oldSurfaceData) {
        SurfaceDataPart data = new SurfaceDataPart(parent);
        data.parent.updateTime = System.currentTimeMillis();
        ChunkPos pos = chunk.getPos();
        //TODO: replace with chunk section stuff to not use a MutableBlockPos at all (see baritone)
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        Registry<Biome> biomeRegistry = level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
        LayerLightEventListener light = getBlockLightLayer(level);
        if (level.dimensionType().hasCeiling()) {
            int ceiling = level.dimensionType().logicalHeight() - 1;
            for (int i = 0; i < 256; ++i) {
                int x = (i >> 4) % 16;
                int z = i % 16;
                blockPos.set((pos.x << 4) + x, 0, (pos.z << 4) + z);
                boolean air = false;
                for (int j = ceiling; j > level.getMinBuildHeight(); --j) {
                    Block block = chunk.getBlockState(blockPos.setY(j)).getBlock();
                    if (block instanceof AirBlock) {
                        air = true;
                    } else if (air) {
                        data.heightmap[i] = j;
                        data.blockid[i] = parent.getOrRegisterResourceLocation(Registry.BLOCK.getKey(block));
                        data.biomeid[i] = parent.getOrRegisterResourceLocation(biomeRegistry.getKey(chunk.getBiomes().getNoiseBiome(x >> 2, data.heightmap[i] >> 2, z >> 2)));
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
                Block top = chunk.getBlockState(blockPos.set((pos.x << 4) + x, data.heightmap[i], (pos.z << 4) + z)).getBlock();
                data.blockid[i] = parent.getOrRegisterResourceLocation(Registry.BLOCK.getKey(top));
                data.biomeid[i] = parent.getOrRegisterResourceLocation(biomeRegistry.getKey(chunk.getBiomes().getNoiseBiome(x >> 2, data.heightmap[i] >> 2, z >> 2)));
 //                data.biomeid[i] = data.getOrRegisterResourceLocation(biomeRegistry.getKey(level.getBiome(blockPos)));
                data.blocklight[i] = (byte) light.getLightValue(blockPos.setY(data.heightmap[i] + 1));

                if (top.equals(Blocks.WATER)) {
                    Block b = top;
                    while (b.equals(Blocks.WATER))
                        b = chunk.getBlockState(blockPos.setY(blockPos.getY() - 1)).getBlock();
                    data.oceanFloorHeightmap[i] = blockPos.getY();
                    data.oceanFloorBlockid[i] = parent.getOrRegisterResourceLocation(Registry.BLOCK.getKey(chunk.getBlockState(blockPos).getBlock()));
                }
            }
        }

        // update south heightmap
        UpdateNorthHeightmapStrategy.UPDATE_EVENT.invoker().onUpdate(parent.south(), data.heightmap);
        UpdateSouthHeightmapStrategy.UPDATE_EVENT.invoker().onUpdate(parent.north(), data.heightmap);

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
