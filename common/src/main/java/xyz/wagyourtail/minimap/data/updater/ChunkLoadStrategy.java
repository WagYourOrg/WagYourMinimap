package xyz.wagyourtail.minimap.data.updater;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import xyz.wagyourtail.minimap.data.ChunkData;

public class ChunkLoadStrategy extends AbstractChunkUpdateStrategy {

    public static Event<Load> LOAD = EventFactory.createLoop();

    public ChunkLoadStrategy() {
        super(1);
    }

    public static ChunkData loadFromChunk(ChunkAccess chunk, Level level, ChunkData oldData) {
        ChunkData data = new ChunkData();
        data.updateTime = System.currentTimeMillis();
        ChunkPos pos = chunk.getPos();
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        Registry<Biome> biomeRegistry = level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
        LayerLightEventListener light = getBlockLightLayer(level);
        for (int i = 0; i < 256; ++i) {
            int x = (i >> 4) % 16;
            int z = i % 16;
            data.heightmap[i] = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
            Block top = chunk.getBlockState(blockPos.set((pos.x << 4) + x, data.heightmap[i], (pos.z << 4) + z)).getBlock();
            data.blockid[i] = data.getOrRegisterResourceLocation(Registry.BLOCK.getKey(top));
            data.biomeid[i] = data.getOrRegisterResourceLocation(biomeRegistry.getKey(level.getBiome(blockPos)));
            data.blocklight[i] = (byte) light.getLightValue(blockPos.setY(data.heightmap[i] + 1));

            if (top.equals(Blocks.WATER)) {
                Block b = top;
                while (b.equals(Blocks.WATER)) b = chunk.getBlockState(blockPos.setY(blockPos.getY() - 1)).getBlock();
                data.oceanFloorHeightmap[i] = blockPos.getY();
                data.oceanFloorBlockid[i] = data.getOrRegisterResourceLocation(Registry.BLOCK.getKey(chunk.getBlockState(blockPos).getBlock()));
            }
        }
        if (oldData != null) oldData.combineWithNewData(data);
        else {
            data.markDirty();
            return data;
        }
        return oldData;
    }

    @Override
    protected void registerEventListener() {
        LOAD.register((chunk, level) -> {
            ChunkPos pos = chunk.getPos();
            updateChunk(
                getChunkLocation(level, pos),
                (region, oldData) -> loadFromChunk(chunk, level, oldData)
            );
        });
    }

    public interface Load {
        void onLoadChunk(ChunkAccess chunk, Level level);
    }

}
