package xyz.wagyourtail.minimap.map.image.colors;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import xyz.wagyourtail.minimap.map.image.ImageStrategy;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public abstract class BlockColors implements IBlockColors, ImageStrategy {
    protected static final Minecraft minecraft = Minecraft.getInstance();
    protected static final Map<Biome, Integer> waterCache = new ConcurrentHashMap<>();
    public static Predicate<Block> water = Set.of(
        Blocks.WATER,
        Blocks.SEAGRASS,
        Blocks.TALL_SEAGRASS,
        Blocks.KELP_PLANT,
        Blocks.KELP
    )::contains;
    public static Predicate<Block> grass = Set.of(Blocks.GRASS_BLOCK, Blocks.GRASS, Blocks.TALL_GRASS)::contains;
    public static Predicate<Block> leaves = (block) -> block instanceof LeavesBlock;

    @Override
    public boolean isWater(Block block) {
        return water.test(block);
    }

    @Override
    public boolean isGrass(Block block) {
        return grass.test(block);
    }

    @Override
    public boolean isLeaves(Block block) {
        return leaves.test(block);
    }

    @Override
    public int getWaterColor(BlockState block, BlockPos pos, @Nullable Biome biome) {
        if (biome == null) {
            return getBlockColor(block, pos);
        }
        return waterCache.computeIfAbsent(biome, Biome::getWaterColor);
    }

}
