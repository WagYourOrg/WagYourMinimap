package xyz.wagyourtail.oldminimap.scanner.updater;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import xyz.wagyourtail.oldminimap.WagYourMinimap;
import xyz.wagyourtail.oldminimap.scanner.ChunkData;
import xyz.wagyourtail.oldminimap.scanner.MapLevel;
import xyz.wagyourtail.oldminimap.scanner.MapRegion;

public class BlockUpdateStrategy extends AbstractChunkUpdateStrategy {
    public static final Event<BlockUpdate> BLOCK_UPDATE_EVENT = EventFactory.createLoop();

    public ChunkData updateChunkData(Level level, BlockPos pos, ChunkData data) {
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(pos.getX(), 0, pos.getZ());
        int index = ChunkData.blockPosToIndex(pos);
        ChunkAccess chunk = level.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
        Registry<Biome> biomeRegistry = level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
        data.heightmap[index] = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ());
        data.blockid[index] = data.getOrRegisterResourceLocation(Registry.BLOCK.getKey(chunk.getBlockState(blockPos.setY(data.heightmap[index])).getBlock()));
        data.biomeid[index] = data.getOrRegisterResourceLocation(biomeRegistry.getKey(level.getBiome(blockPos)));
        data.blocklight[index] = (byte) level.getBrightness(LightLayer.BLOCK, blockPos.setY(data.heightmap[index] + 1));

        data.oceanFloorHeightmap[index] = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR, pos.getX(), pos.getZ());
        data.oceanFloorBlockid[index] = data.getOrRegisterResourceLocation(Registry.BLOCK.getKey(chunk.getBlockState(blockPos.setY(data.oceanFloorHeightmap[index])).getBlock()));
        data.oceanFloorBiomeid[index] = data.getOrRegisterResourceLocation(biomeRegistry.getKey(level.getBiome(blockPos)));
        return data;
    }

    @Override
    protected void registerEventListener() {
        BLOCK_UPDATE_EVENT.register((pos, level) -> {
            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;
            try {
                updateChunk(
                    WagYourMinimap.INSTANCE.getServerName(),
                    WagYourMinimap.INSTANCE.getLevelName(level),
                    level,
                    new MapLevel.Pos(chunkX >> 5, chunkZ >> 5),
                    MapRegion.chunkPosToIndex(chunkX, chunkZ),
                    ((region, chunkData) -> updateChunkData(level, pos, chunkData))
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
