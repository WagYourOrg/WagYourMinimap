package xyz.wagyourtail.minimap.map.image.colors;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import xyz.wagyourtail.minimap.map.image.ImageStrategy;

public interface IBlockColors extends ImageStrategy {
    boolean isWater(Block block);
    boolean isGrass(Block block);
    boolean isLeaves(Block block);
    int getWaterColor(BlockState block, BlockPos pos, @Nullable Biome biome);
    int getGrassColor(BlockState block, BlockPos pos, @Nullable Biome biome);
    int getLeavesColor(BlockState block, BlockPos pos, @Nullable Biome biome);
    int getBlockColor(BlockState block, BlockPos pos);

    default int colorCombine(int colorA, int colorB, float aRatio) {
        float bRatio = 1.0F - aRatio;
        int red = (int) (((colorA & 0xFF0000) >> 0x10) * aRatio);
        int green = (int) (((colorA & 0xFF00) >> 0x8) * aRatio);
        int blue = (int) ((colorA & 0xFF) * aRatio);
        red += (int) (((colorB & 0xFF0000) >> 0x10) * bRatio);
        green += (int) (((colorB & 0xFF00) >> 0x8) * bRatio);
        blue += (int) ((colorB & 0xFF) * bRatio);
        return red << 0x10 | green << 0x8 | blue;
    }

    default int brightnessForHeight2(int color, int height, int north, int south) {
        int red = (color & 0xFF0000) >> 0x10;
        int green = (color & 0xFF00) >> 0x8;
        int blue = color & 0xFF;
        if (north > height) {
            red = (red * 4 / 5);
            green = (green * 4 / 5);
            blue = (blue * 4 / 5);
        } else if (south <= height) {
            red = (red * 9 / 10);
            green = (green * 9 / 10);
            blue = (blue * 9 / 10);
        }
        return (color & 0xFF000000) | red << 0x10 | green << 0x8 | blue;
    }
}
