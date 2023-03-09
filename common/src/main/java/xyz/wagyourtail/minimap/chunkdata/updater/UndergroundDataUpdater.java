package xyz.wagyourtail.minimap.chunkdata.updater;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.chunkdata.parts.SurfaceDataPart;
import xyz.wagyourtail.minimap.chunkdata.parts.UndergroundDataPart;
import xyz.wagyourtail.minimap.map.MapServer;
import xyz.wagyourtail.minimap.map.image.UndergroundAccurateImageStrategy;
import xyz.wagyourtail.minimap.map.image.UndergroundBlockLightImageStrategy;
import xyz.wagyourtail.minimap.map.image.UndergroundVanillaImageStrategy;

import java.util.Set;

public class UndergroundDataUpdater extends AbstractChunkDataUpdater<UndergroundDataPart> {
    public static int sectionHeight = 4;


    public UndergroundDataUpdater() {
        super(Set.of(
            UndergroundVanillaImageStrategy.class.getCanonicalName(),
            UndergroundAccurateImageStrategy.class.getCanonicalName(),
            UndergroundBlockLightImageStrategy.class.getCanonicalName()
        ));
    }

    @Override
    public void onBlockUpdate(BlockPos pos, Level level) {
        MapServer.MapLevel mapLevel = MinimapApi.getInstance().getMapServer().getLevelFor(level);
        ChunkAccess chunk = level.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false);
        if (chunk == null) {
            return;
        }
        updateChunk(
            getChunkLocation(mapLevel, chunk.getPos()),
            (loc, parent, oldData) -> {
                //TODO: yCol updater like in SurfaceDataUpdater
                if (oldData == null || oldData.sectionHeight != sectionHeight) {
                    return oldData;
                }
                for (int y = (pos.getY() - level.dimensionType().minY()) / sectionHeight;
                     y < oldData.data.length; y++) {
                    if (oldData.data[y] != null) {
                        if (oldData.data[y].heightmap()[SurfaceDataPart.blockPosToIndex(pos)] > pos.getY()) {
                            break;
                        }
                        scanPart(level, chunk, parent, oldData, y);
                    }
                }
                return oldData;
            }
        );
    }

    UndergroundDataPart scanPart(Level level, ChunkAccess chunk, ChunkData parent, UndergroundDataPart data, int sectionY) {
        if (data == null || data.sectionHeight != sectionHeight) {
            data = new UndergroundDataPart(parent);
        }
        ChunkPos pos = chunk.getPos();
        LayerLightEventListener light = getBlockLightLayer(level);
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        Registry<Biome> biomeRegistry = level.registryAccess().registryOrThrow(Registries.BIOME);

        int sectionMin = sectionY * sectionHeight;
        int sectionMax = sectionMin + sectionHeight;

        UndergroundDataPart.Data d = data.data[sectionY];
        if (d == null) {
            d = new UndergroundDataPart.Data(new int[256], new int[256], new byte[256], new int[256]);
            data.data[sectionY] = d;
        }

        int[] blockid = d.blockid();
        int[] heightmap = d.heightmap();
        int[] biomeid = d.biomeid();
        byte[] lightmap = d.lightmap();

        int minHeight = level.dimensionType().minY();

        for (int i = 0; i < 256; ++i) {
            int x = (i >> 4) % 16;
            int z = i % 16;
            blockPos.set((pos.x << 4) + x, 0, (pos.z << 4) + z);
            boolean air = false;
            for (int y = sectionMax + minHeight; y >= sectionMin + minHeight; --y) {
                BlockState state = chunk.getBlockState(blockPos.setY(y));
                if (state.isAir() || !state.getFluidState().isEmpty()) {
                    air = true;
                    break;
                }
            }
            if (air) {
                int y = blockPos.getY();
                while (y > minHeight) {
                    BlockState state = chunk.getBlockState(blockPos.setY(--y));
                    if (!state.isAir()) {
                        blockid[i] = data.getOrRegisterBlockState(state);
                        heightmap[i] = y;
                        biomeid[i] = data.getOrRegisterBiome(biomeRegistry.getKey(
                            chunk.getNoiseBiome(x >> 2, y >> 2, z >> 2).value()
                        ));
                        lightmap[i] = (byte) light.getLightValue(blockPos.setY(y + 1));
                        break;
                    }
                }
            } else {
                blockid[i] = 0;
                heightmap[i] = sectionMax + minHeight;
                biomeid[i] = 0;
                lightmap[i] = 0;
            }

        }

        parent.invalidateDerivitives(derivitivesToInvalidate);
        return data;
    }

    @Override
    public void onLoadChunk(ChunkAccess chunk, Level level) {
        //NOPE
    }

    @Override
    public Class<UndergroundDataPart> getType() {
        return UndergroundDataPart.class;
    }

    public void scan(Level level, ChunkAccess chunk, int sectionY) {
        MapServer.MapLevel mapLevel = MinimapApi.getInstance().getMapServer().getLevelFor(level);
        updateChunk(
            getChunkLocation(mapLevel, chunk.getPos()),
            (loc, parent, oldData) -> scanPart(level, chunk, parent, oldData, sectionY)
        );
    }

    public void scan(Level level, ChunkLocation location, int sectionY) {
        ChunkAccess chunk = level.getChunk(location.getChunkX(), location.getChunkZ(), ChunkStatus.FULL, false);
        if (chunk != null) {
            updateChunk(
                location,
                (loc, parent, oldData) -> scanPart(level, chunk, parent, oldData, sectionY)
            );
        }
    }

}
