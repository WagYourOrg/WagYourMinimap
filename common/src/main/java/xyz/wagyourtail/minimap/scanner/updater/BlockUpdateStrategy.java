package xyz.wagyourtail.minimap.scanner.updater;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.client.gui.image.AbstractImageStrategy;
import xyz.wagyourtail.minimap.scanner.ChunkData;
import xyz.wagyourtail.minimap.scanner.MapLevel;
import xyz.wagyourtail.minimap.scanner.MapRegion;

public class BlockUpdateStrategy extends AbstractChunkUpdateStrategy {
    public static final Event<BlockUpdate> BLOCK_UPDATE_EVENT = EventFactory.createLoop();

    public ChunkData updateChunkData(Level level, BlockPos pos, ChunkData data, AbstractImageStrategy.ChunkLocation loc) {
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
        if (invalidateOld && MinimapApi.getInstance() instanceof MinimapClientApi inst) inst.invalidateImages(loc);
        return data;
    }

    @Override
    protected void registerEventListener() {
        BLOCK_UPDATE_EVENT.register((pos, level) -> {
            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;
            MapLevel.Pos regionPos = new MapLevel.Pos(chunkX >> 5, chunkZ >> 5);
            try {
                updateChunk(
                    MinimapApi.getInstance().getServerName(),
                    MinimapClientApi.getInstance().getLevelName(level),
                    level,
                    regionPos,
                    MapRegion.chunkPosToIndex(chunkX, chunkZ),
                    ((region, chunkData) -> updateChunkData(level, pos, chunkData, new AbstractImageStrategy.ChunkLocation(region.parent, regionPos, MapRegion.chunkPosToIndex(chunkX, chunkZ))))
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
