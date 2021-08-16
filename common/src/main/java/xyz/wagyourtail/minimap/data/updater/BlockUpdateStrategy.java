package xyz.wagyourtail.minimap.data.updater;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import xyz.wagyourtail.minimap.data.ChunkData;

public class BlockUpdateStrategy extends AbstractChunkUpdateStrategy {
    public static final Event<BlockUpdate> BLOCK_UPDATE_EVENT = EventFactory.createLoop();

    public BlockUpdateStrategy() {
        super(1);
    }

    public ChunkData updateChunkData(Level level, ChunkAccess chunk, BlockPos pos, ChunkData data) {
        if (data == null) return null;
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(pos.getX(), 0, pos.getZ());
        int index = ChunkData.blockPosToIndex(pos);
        Registry<Biome> biomeRegistry = level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
        int newHeight = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ());
        data.heightmap[index] = newHeight;
        int newBlock = data.getOrRegisterResourceLocation(Registry.BLOCK.getKey(chunk.getBlockState(blockPos.setY(data.heightmap[index])).getBlock()));
        data.blockid[index] = newBlock;
        data.biomeid[index] = data.getOrRegisterResourceLocation(biomeRegistry.getKey(level.getBiome(blockPos)));

        int newOceanFloorHeight = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR, pos.getX(), pos.getZ());
        data.oceanFloorHeightmap[index] = newOceanFloorHeight;
        int newOceanBlockId = data.getOrRegisterResourceLocation(Registry.BLOCK.getKey(chunk.getBlockState(blockPos.setY(data.oceanFloorHeightmap[index])).getBlock()));
        data.oceanFloorBlockid[index] = newOceanBlockId;
        data.updateTime = System.currentTimeMillis();
        updateNeighborLighting(level, getBlockLightLayer(level), pos.getX() >> 4, pos.getZ() >> 4);
        return data;
    }

    public void updateNeighborLighting(Level level, LayerLightEventListener light, int chunkX, int chunkZ) {
        for (int i = chunkX - 1; i < chunkX + 2; ++i) {
            for (int j = chunkZ - 1; j < chunkZ + 2; ++j) {
                if (level.hasChunk(i, j)) {
                    int finalI = i;
                    int finalJ = j;
                    ChunkAccess chunk = level.getChunk(finalI, finalJ);
                    if (chunk == null) continue;
                    updateChunk(
                        getChunkLocation(level, finalI, finalJ),
                        (region, chunkData) -> updateLighting(level, chunk, light, chunkData, finalI, finalJ)
                    );
                }
            }
        }
    }

    public ChunkData updateLighting(Level level, ChunkAccess chunk, LayerLightEventListener light, ChunkData oldData, int chunkX, int chunkZ) {
        if (oldData == null) return null;
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(0, 0, 0);
        if (chunk != null) {
            boolean invalidate = false;
            for (int i = 0; i < 256; ++i) {
                int x = (i >> 4) % 16;
                int z = i % 16;
                byte newBlockLight = (byte) light.getLightValue(blockPos.set((chunkX << 4) + x, oldData.heightmap[i] + 1, (chunkZ << 4) + z));
                invalidate = invalidate || newBlockLight != oldData.blocklight[i];
                oldData.blocklight[i] = newBlockLight;
            }
            if (invalidate) oldData.markDirty();
        }
        return oldData;
    }

    @Override
    protected void registerEventListener() {
        BLOCK_UPDATE_EVENT.register((pos, level) -> {
            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;
            ChunkAccess chunk = level.getChunk(chunkX, chunkZ);
            try {
                updateChunk(
                    getChunkLocation(level, chunkX, chunkZ),
                    ((location, chunkData) -> updateChunkData(level, chunk, pos, chunkData))
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    public interface BlockUpdate {
        void onBlockUpdate(BlockPos pos, Level level);
    }
}
