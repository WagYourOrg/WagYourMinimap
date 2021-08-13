package xyz.wagyourtail.minimap.scanner.updater;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.client.gui.image.AbstractImageStrategy;
import xyz.wagyourtail.minimap.scanner.ChunkData;
import xyz.wagyourtail.minimap.scanner.MapLevel;
import xyz.wagyourtail.minimap.scanner.MapRegion;

public class ChunkLoadStrategy extends AbstractChunkUpdateStrategy {

    public static Event<Load> LOAD = EventFactory.createLoop();

    public synchronized ChunkData loadFromChunk(ChunkAccess chunk, Level level, MapRegion parent, ChunkData oldData, AbstractImageStrategy.ChunkLocation loc) {
        ChunkData data = new ChunkData(parent);
        data.updateTime = System.currentTimeMillis();
        ChunkPos pos = chunk.getPos();
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        Registry<Biome> biomeRegistry = level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
        for (int i = 0; i < 256; ++i) {
            int x = (i >> 4) % 16;
            int z = i % 16;
            data.heightmap[i] = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
            Block top = chunk.getBlockState(blockPos.set((pos.x << 4) + x, data.heightmap[i], (pos.z << 4) + z)).getBlock();
            data.blockid[i] = data.getOrRegisterResourceLocation(Registry.BLOCK.getKey(top));
            data.biomeid[i] = data.getOrRegisterResourceLocation(biomeRegistry.getKey(level.getBiome(blockPos)));
            data.blocklight[i] = (byte) level.getBrightness(LightLayer.BLOCK, blockPos.setY(data.heightmap[i] + 1));

            if (top.equals(Blocks.WATER)) {
                Block b = top;
                while (b.equals(Blocks.WATER)) b = chunk.getBlockState(blockPos.setY(blockPos.getY() - 1)).getBlock();
                data.oceanFloorHeightmap[i] = blockPos.getY();
                data.oceanFloorBlockid[i] = data.getOrRegisterResourceLocation(Registry.BLOCK.getKey(chunk.getBlockState(blockPos).getBlock()));
                data.oceanFloorBiomeid[i] = data.getOrRegisterResourceLocation(biomeRegistry.getKey(level.getBiome(blockPos)));
            }
        }
        if (oldData != null) data.copyDerivatives(oldData);
        return data;
    }

    @Override
    protected void registerEventListener() {
        LOAD.register((chunk, level) -> {
            String server_slug = MinimapApi.getInstance().getServerName();
            String level_slug = MinimapApi.getInstance().getLevelName(level);
            ChunkPos pos = chunk.getPos();
            int index = MapRegion.chunkPosToIndex(pos);
            MapLevel.Pos region_pos = new MapLevel.Pos(pos.getRegionX(), pos.getRegionZ());
            updateChunk(server_slug,
                level_slug,
                level,
                region_pos,
                index,
                (region, oldData) -> loadFromChunk(chunk, level, region, oldData, new AbstractImageStrategy.ChunkLocation(region.parent, region_pos, index))
            );
        });
    }

    public interface Load {
        void onLoadChunk(ChunkAccess chunk, Level level);
    }

}
