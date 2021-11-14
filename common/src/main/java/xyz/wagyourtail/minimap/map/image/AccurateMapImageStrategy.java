package xyz.wagyourtail.minimap.map.image;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class AccurateMapImageStrategy extends VanillaMapImageStrategy {

    private static final Random random = new Random();
    protected Map<BlockState, Integer> blockColorCache = new ConcurrentHashMap<>();

    @Override
    public int getWaterColor(BlockState block, BlockPos pos, @Nullable Biome biome) {
        return super.getWaterColor(block, pos, biome);
    }

    @Override
    public int getBlockColor(BlockState block, BlockPos pos) {
        return blockColorCache.computeIfAbsent(block, (b) -> {
            BlockState state = b;
            random.setSeed(pos.asLong());
            List<BakedQuad> quads = minecraft.getBlockRenderer().getBlockModel(state).getQuads(state,
                Direction.UP,
                random
            );
            if (quads.isEmpty()) {
                return super.getBlockColor(b, pos);
            } else {
                //TODO: figure the rest of this shit out
                for (BakedQuad quad : quads) {
                    //                    quad.getSprite()
                }
            }
            return 0;
        });
    }

    @Override
    public int getLeavesColor(BlockState block, BlockPos pos, @Nullable Biome biome) {
        return super.getLeavesColor(block, pos, biome);
    }

    @Override
    public int getGrassColor(BlockState block, BlockPos pos, @Nullable Biome biome) {
        return super.getGrassColor(block, pos, biome);
    }

}
