package xyz.wagyourtail.minimap.data.updater;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import xyz.wagyourtail.minimap.data.ChunkData;

public class BlockUpdateStrategy extends AbstractChunkUpdateStrategy {
    public static final Event<BlockUpdate> BLOCK_UPDATE_EVENT = EventFactory.createLoop();

    public ChunkData updateChunkData(Level level, BlockPos pos, ChunkData data) {
        if (data == null) return null;
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(pos.getX(), 0, pos.getZ());
        int index = ChunkData.blockPosToIndex(pos);
        ChunkAccess chunk = level.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
        Registry<Biome> biomeRegistry = level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
        int newHeight = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ());
        boolean invalidateOld = data.heightmap[index] != newHeight;
        data.heightmap[index] = newHeight;
        int newBlock = data.getOrRegisterResourceLocation(Registry.BLOCK.getKey(chunk.getBlockState(blockPos.setY(data.heightmap[index])).getBlock()));
        invalidateOld = invalidateOld || data.blockid[index] != newBlock;
        data.blockid[index] = newBlock;
        data.biomeid[index] = data.getOrRegisterResourceLocation(biomeRegistry.getKey(level.getBiome(blockPos)));
        byte newBlockLight = (byte) level.getBrightness(LightLayer.BLOCK, blockPos.setY(data.heightmap[index] + 1));
        invalidateOld = invalidateOld || data.blocklight[index] != newBlockLight;
        data.blocklight[index] = newBlockLight;

        int newOceanFloorHeight = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR, pos.getX(), pos.getZ());
        invalidateOld = invalidateOld || data.oceanFloorHeightmap[index] != newOceanFloorHeight;
        data.oceanFloorHeightmap[index] = newOceanFloorHeight;
        int newOceanBlockId = data.getOrRegisterResourceLocation(Registry.BLOCK.getKey(chunk.getBlockState(blockPos.setY(data.oceanFloorHeightmap[index])).getBlock()));
        invalidateOld = invalidateOld || data.oceanFloorBlockid[index] != newOceanBlockId;
        data.oceanFloorBlockid[index] = newOceanBlockId;
        data.oceanFloorBiomeid[index] = data.getOrRegisterResourceLocation(biomeRegistry.getKey(level.getBiome(blockPos)));
        data.updateTime = System.currentTimeMillis();
        updateLighting(level, data, pos.getX() >> 4, pos.getZ() >> 4);
        if (invalidateOld) data.invalidateDerivitives();
        return data;
    }

    public void updateNeighborLighting(Level level, int chunkX, int chunkZ) {
        for (int i = chunkX - 1; i < chunkX + 2; ++i) {
            for (int j = chunkZ - 1; j < chunkZ + 2; ++j) {
                if (level.hasChunk(i, j)) {
                    int finalI = i;
                    int finalJ = j;
                    updateChunk(
                        getChunkLocation(level, finalI, finalJ),
                        (region, chunkData) -> updateLighting(level, chunkData, finalI, finalJ)
                    );
                }
            }
        }
    }

    public ChunkData updateLighting(Level level, ChunkData oldData, int chunkX, int chunkZ) {
        if (oldData == null) return null;
        ChunkAccess chunk = level.getChunkSource().getChunk(chunkX, chunkZ, false);
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(0, 0, 0);
        if (chunk != null) {
            boolean invalidate = false;
            for (int i = 0; i < 256; ++i) {
                int x = (i >> 4) % 16;
                int z = i % 16;

                byte newBlockLight = (byte) level.getBrightness(LightLayer.BLOCK, blockPos.set((chunkX << 4) + x, oldData.heightmap[i] + 1, (chunkZ << 4) + z));
                invalidate = invalidate || newBlockLight != oldData.blocklight[i];
                oldData.blocklight[i] = newBlockLight;
            }
            if (invalidate) oldData.invalidateDerivitives();
        }
        return oldData;
    }

    @Override
    protected void registerEventListener() {
        BLOCK_UPDATE_EVENT.register((pos, level) -> {
            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;
            try {
                updateChunk(
                    getChunkLocation(level, chunkX, chunkZ),
                    ((location, chunkData) -> updateChunkData(level, pos, chunkData))
                );
                updateNeighborLighting(level, chunkX, chunkZ);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    public interface BlockUpdate {
        void onBlockUpdate(BlockPos pos, Level level);
    }
}
