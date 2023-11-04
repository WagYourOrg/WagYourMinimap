package xyz.wagyourtail.minimap.chunkdata.updater;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.chunkdata.parts.SurfaceDataPart;
import xyz.wagyourtail.minimap.map.MapServer;
import xyz.wagyourtail.minimap.map.image.AccurateMapImageStrategy;
import xyz.wagyourtail.minimap.map.image.SurfaceBlockLightImageStrategy;
import xyz.wagyourtail.minimap.map.image.VanillaMapImageStrategy;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

public class SurfaceDataUpdater extends AbstractChunkDataUpdater<SurfaceDataPart> {

    public SurfaceDataUpdater() {
        super(Set.of(
            VanillaMapImageStrategy.class.getCanonicalName(),
            AccurateMapImageStrategy.class.getCanonicalName(),
            SurfaceBlockLightImageStrategy.class.getCanonicalName()
        ));
    }

    @Override
    public void onLoadChunk(ChunkAccess chunk, Level level) {
        MapServer.MapLevel mapLevel = MinimapApi.getInstance().getMapServer().getLevelFor(level);
        ChunkPos pos = chunk.getPos();
        updateChunk(
            getChunkLocation(mapLevel, pos),
            (location, parent, oldData) -> loadFromChunk(chunk, mapLevel, level, parent, oldData)
        );
    }

    public SurfaceDataPart loadFromChunk(ChunkAccess chunk, MapServer.MapLevel level, Level mclevel, ChunkData parent, SurfaceDataPart oldSurfaceData) {
        SurfaceDataPart data = new SurfaceDataPart(parent);
        data.parent.updateTime = System.currentTimeMillis();
        ChunkPos pos = chunk.getPos();
        //TODO: replace with chunk section stuff to not use a MutableBlockPos at all (see baritone), maybe not possible since we need light levels too
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        Registry<Biome> biomeRegistry = mclevel.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
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
                        data.biomeid[i] = parent.getOrRegisterBiome(biomeRegistry.getKey(chunk
                            .getBiomes().getNoiseBiome(x >> 2, data.heightmap[i] >> 2, z >> 2)
                        ));
                        break;
                    }
                }
            }
        } else {
            for (int i = 0; i < 256; ++i) {
                int x = (i >> 4) % 16;
                int z = i % 16;
                data.heightmap[i] = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                BlockState top = chunk.getBlockState(blockPos.set(
                    (pos.x << 4) + x,
                    data.heightmap[i],
                    (pos.z << 4) + z
                ));
                data.blockid[i] = parent.getOrRegisterBlockState(top);
                data.biomeid[i] = parent.getOrRegisterBiome(biomeRegistry.getKey(chunk
                    .getBiomes().getNoiseBiome(x >> 2, data.heightmap[i] >> 2, z >> 2)
                ));

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

        if (oldSurfaceData != null) {
            if (oldSurfaceData.mergeFrom(data)) {
                parent.invalidateDerivitives(derivitivesToInvalidate);
            }
            return oldSurfaceData;
        } else {
            parent.invalidateDerivitives(derivitivesToInvalidate);
        }
        parent.markDirty();
        return data;
    }

    @Override
    public void onBlockUpdate(BlockPos pos, Level level) {
        MapServer.MapLevel mapLevel = MinimapApi.getInstance().getMapServer().getLevelFor(level);
        ChunkAccess chunk = level.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false);
        if (chunk == null) {
            return;
        }
        updateChunk(
            getChunkLocation(mapLevel, pos.getX() >> 4, pos.getZ() >> 4),
            (location, parent, oldData) -> updateYCol(parent, oldData, chunk, mapLevel, level, pos)
        );
    }

    public SurfaceDataPart updateYCol(ChunkData parent, SurfaceDataPart data, ChunkAccess chunk, MapServer.MapLevel level, Level mclevel, BlockPos bp) {
        if (data == null) {
            return loadFromChunk(chunk, level, mclevel, parent, data);
        }
        data.parent.updateTime = System.currentTimeMillis();
        Registry<Biome> biomeRegistry = mclevel.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
        int x = bp.getX();
        int z = bp.getZ();
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(x, 0, z);
        int i = SurfaceDataPart.blockPosToIndex(blockPos);
        if (mclevel.dimensionType().hasCeiling()) {
            int ceiling = mclevel.dimensionType().logicalHeight() - 1;
            boolean air = false;
            for (int j = ceiling; j > mclevel.getMinBuildHeight(); --j) {
                BlockState block = chunk.getBlockState(blockPos.setY(j));
                if (block.getBlock() instanceof AirBlock) {
                    air = true;
                } else if (air) {
                    data.heightmap[i] = j;
                    data.blockid[i] = parent.getOrRegisterBlockState(block);
                    data.biomeid[i] = parent.getOrRegisterBiome(biomeRegistry.getKey(
                        chunk.getBiomes().getNoiseBiome(x >> 2, data.heightmap[i] >> 2, z >> 2)
                    ));
                    break;
                }
            }
        } else {
            data.heightmap[i] = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
            BlockState top = chunk.getBlockState(blockPos.setY(data.heightmap[i]));
            data.blockid[i] = parent.getOrRegisterBlockState(top);
            data.biomeid[i] = parent.getOrRegisterBiome(biomeRegistry.getKey(
                chunk.getBiomes().getNoiseBiome(x >> 2, data.heightmap[i] >> 2, z >> 2)
            ));
            if (top.getBlock().equals(Blocks.WATER)) {
                BlockState b = top;
                while (b.getBlock().equals(Blocks.WATER)) {
                    b = chunk.getBlockState(blockPos.setY(blockPos.getY() - 1));
                }
                data.oceanFloorHeightmap[i] = blockPos.getY();
                data.oceanFloorBlockid[i] = parent.getOrRegisterBlockState(chunk.getBlockState(blockPos));
            }
        }
        parent.markDirty();
        parent.invalidateDerivitives(derivitivesToInvalidate);
        return data;
    }

    @Override
    public Class<SurfaceDataPart> getType() {
        return SurfaceDataPart.class;
    }

}
