package xyz.wagyourtail.minimap.map.image.colors;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class VanillaBlockColors extends BlockColors {
    protected static final Map<Biome, Integer> grassCache = new ConcurrentHashMap<>();
    protected static final Map<Biome, Integer> foliageCache = new ConcurrentHashMap<>();

    @Override
    public int getBlockColor(BlockState block, BlockPos pos) {
        return block.getMapColor(Minecraft.getInstance().level, pos).col;
    }

    @Override
    public int getLeavesColor(BlockState block, BlockPos pos, @Nullable Biome biome) {
        if (biome == null) {
            return getBlockColor(block, pos);
        }
        return foliageCache.computeIfAbsent(biome, Biome::getFoliageColor);
    }

    @Override
    public int getGrassColor(BlockState block, BlockPos pos, @Nullable Biome biome) {
        if (biome == null) {
            return getBlockColor(block, pos);
        } else {
            return grassCache.computeIfAbsent(biome, b -> b.getGrassColor(0, 0));
        }
    }
}
