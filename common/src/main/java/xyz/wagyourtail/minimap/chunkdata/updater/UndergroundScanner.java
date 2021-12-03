package xyz.wagyourtail.minimap.chunkdata.updater;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.chunkdata.parts.SurfaceDataPart;
import xyz.wagyourtail.minimap.chunkdata.parts.UndergroundDataPart;
import xyz.wagyourtail.minimap.map.MapServer;

public class UndergroundScanner extends AbstractChunkUpdateStrategy<UndergroundDataPart> {
    public static int sectionHeight = 4;

    @Override
    protected void registerEventListener() {
//        BlockUpdateStrategy.BLOCK_UPDATE_EVENT.register((pos, level) -> {
//            if (level != mc.level) {
//                return;
//            }
//            MapServer.MapLevel mapLevel = MinimapApi.getInstance().getMapServer().getCurrentLevel();
//            ChunkAccess chunk = level.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false);
//            if (chunk == null) {
//                return;
//            }
//            updateChunk(
//                getChunkLocation(mapLevel, chunk.getPos()),
//                (loc, parent, oldData) -> {
//                    if (oldData == null || oldData.sectionHeight != sectionHeight) {
//                        return oldData;
//                    }
//                    for (int y = pos.getY() / sectionHeight; y < oldData.data.length; y++) {
//                        if (oldData.data[y] != null) {
//                            if (oldData.data[y].heightmap()[SurfaceDataPart.blockPosToIndex(pos)] > pos.getY()) break;
//                            scanPart(level, chunk, parent, oldData, y);
//                        }
//                    }
//                    return oldData;
//                }
//            );
//        });
    }

    @Override
    public Class<UndergroundDataPart> getType() {
        return UndergroundDataPart.class;
    }

    UndergroundDataPart scanPart(Level level, ChunkAccess chunk, ChunkData parent, UndergroundDataPart data, int sectionY) {
        if (data == null || data.sectionHeight != sectionHeight) {
            data = new UndergroundDataPart(parent);
        }
        ChunkPos pos = chunk.getPos();
        LayerLightEventListener light = getBlockLightLayer(level);
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        Registry<Biome> biomeRegistry = level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);

        int sectionMin = sectionY * sectionHeight;
        int sectionMax = sectionMin + sectionHeight;

        UndergroundDataPart.Data d = data.data[sectionY];
        if (d == null) {
            d = new UndergroundDataPart.Data(new int[256], new int[256], new int[256], new int[256]);
            data.data[sectionY] = d;
        }

        int[] blockid = d.blockid();
        int[] heightmap = d.heightmap();
        int[] biomeid = d.biomeid();
        int[] lightmap = d.lightmap();


        for (int i = 0; i < 256; ++i) {
            int x = (i >> 4) % 16;
            int z = i % 16;
            blockPos.set((pos.x << 4) + x, 0, (pos.z << 4) + z);
            boolean air = false;
            for (int y = sectionMax; y >= sectionMin; --y) {
                if (chunk.getBlockState(blockPos.setY(y)).isAir()) {
                    air = true;
                    break;
                }
            }
            if (air) {
                int y = blockPos.getY();
                while (y > chunk.getMinBuildHeight()) {
                    BlockState state = chunk.getBlockState(blockPos.setY(--y));
                    if (!state.isAir()) {
                        blockid[i] = data.getOrRegisterBlockState(state);
                        heightmap[i] = y;
                        biomeid[i] = data.getOrRegisterBiome(biomeRegistry.getKey(chunk
                            .getNoiseBiome(x >> 2, y >> 2, z >> 2)
                        ));
                        lightmap[i] = light.getLightValue(blockPos.setY(y + 1));
                        break;
                    }
                }
            } else {
                blockid[i] = 0;
                heightmap[i] = sectionMax;
                biomeid[i] = 0;
                lightmap[i] = 0;
            }

        }

        parent.invalidateDerivitives();
        return data;
    }

    public void scan(Level level, ChunkAccess chunk, int sectionY) {
        MapServer.MapLevel mapLevel = MinimapApi.getInstance().getMapServer().getCurrentLevel();
        updateChunk(
            getChunkLocation(mapLevel, chunk.getPos()),
            (loc, parent, oldData) -> scanPart(level, chunk, parent, oldData, sectionY)
        );
    }

}
