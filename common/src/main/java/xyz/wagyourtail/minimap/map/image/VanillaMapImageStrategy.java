package xyz.wagyourtail.minimap.map.image;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import xyz.wagyourtail.minimap.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.chunkdata.parts.SurfaceDataPart;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class VanillaMapImageStrategy extends AbstractImageStrategy {
    protected static final Map<Biome, Integer> grassCache = new ConcurrentHashMap<>();
    protected static final Map<Biome, Integer> foliageCache = new ConcurrentHashMap<>();
    protected static final Map<Biome, Integer> waterCache = new ConcurrentHashMap<>();
    public final static Predicate<Block> water = Set.of(Blocks.WATER,
        Blocks.SEAGRASS,
        Blocks.TALL_SEAGRASS,
        Blocks.KELP_PLANT,
        Blocks.KELP
    )::contains;
    public final static Predicate<Block> grass = Set.of(Blocks.GRASS_BLOCK, Blocks.GRASS, Blocks.TALL_GRASS)::contains;
    public final static Predicate<Block> leaves = (block) -> block instanceof LeavesBlock;

    @Override
    public DynamicTexture load(ChunkLocation location, ChunkData data) {
        SurfaceDataPart surface = data.getData(SurfaceDataPart.class).orElse(null);
        if (surface == null) {
            return null;
        }
        NativeImage image = new NativeImage(16, 16, false);
        assert minecraft.level != null;
        Registry<Biome> biomeRegistry = minecraft.level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
        int[] north = data.north()
            .get()
            .getData(SurfaceDataPart.class)
            .map(e -> e.heightmap)
            .orElseGet(() -> new int[256]);
        int[] south = data.south()
            .get()
            .getData(SurfaceDataPart.class)
            .map(e -> e.heightmap)
            .orElseGet(() -> new int[256]);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        int i;
        int chunkX = location.getChunkX() << 4;
        int chunkZ = location.getChunkZ() << 4;
        for (i = 0; i < 256; ++i) {
            int x = (i >> 4) % 16;
            int z = i % 16;
            pos.set(chunkX | x, surface.heightmap[i], chunkZ | z);
            Biome biome = biomeRegistry.get(data.getBiome(surface.biomeid[i]));
            BlockState state = data.getBlockState(surface.blockid[i]);
            int color;
            if (water.test(state.getBlock())) {
                int waterColor = getWaterColor(state, pos, biome);
                state = data.getBlockState(surface.oceanFloorBlockid[i]);
                float waterRatio = Math.min(
                    // 0.7 - 1.0 depending on depth of water 1 - 10 blocks...
                    .7f + .3F * (surface.heightmap[i] - surface.oceanFloorHeightmap[i]) / 10F, 1.0F);
                color = colorCombine(waterColor, getBlockColor(state, pos), waterRatio);
            } else if (grass.test(state.getBlock())) {
                color = getGrassColor(state, pos, biome);
            } else if (leaves.test(state.getBlock())) {
                color = getLeavesColor(state, pos, biome);
            } else {
                color = getBlockColor(state, pos);
            }
            if (z == 0) {
                color = brightnessForHeight2(color, surface.heightmap[i], north[15 + 16 * x], surface.heightmap[i + 1]);
            } else if (z == 15) {
                color = brightnessForHeight2(color, surface.heightmap[i], surface.heightmap[i - 1], south[16 * x]);
            } else {
                color = brightnessForHeight2(color,
                    surface.heightmap[i],
                    surface.heightmap[i - 1],
                    surface.heightmap[i + 1]
                );
            }
            image.setPixelRGBA(x, z, 0xFF000000 | colorFormatSwap(color));
        }
        return new DynamicTexture(image);
    }

    public int getWaterColor(BlockState block, BlockPos pos, @Nullable Biome biome) {
        if (biome == null) {
            return getBlockColor(block, pos);
        }
        return waterCache.computeIfAbsent(biome, Biome::getWaterColor);
    }

    public int getBlockColor(BlockState block, BlockPos pos) {
        return block.getMapColor(Minecraft.getInstance().level, pos).col;

    }

    public int getLeavesColor(BlockState block, BlockPos pos, @Nullable Biome biome) {
        if (biome == null) {
            return getBlockColor(block, pos);
        }
        return foliageCache.computeIfAbsent(biome, Biome::getFoliageColor);
    }


    public int getGrassColor(BlockState block, BlockPos pos, @Nullable Biome biome) {
        if (biome == null) {
            return getBlockColor(block, pos);
        } else {
            return grassCache.computeIfAbsent(biome, b -> b.getGrassColor(0, 0));
        }
    }


    private int colorCombine(int colorA, int colorB, float aRatio) {
        float bRatio = 1.0F - aRatio;
        int red = (int) (((colorA & 0xFF0000) >> 0x10) * aRatio);
        int green = (int) (((colorA & 0xFF00) >> 0x8) * aRatio);
        int blue = (int) ((colorA & 0xFF) * aRatio);
        red += (int) (((colorB & 0xFF0000) >> 0x10) * bRatio);
        green += (int) (((colorB & 0xFF00) >> 0x8) * bRatio);
        blue += (int) ((colorB & 0xFF) * bRatio);
        return red << 0x10 | green << 0x8 | blue;
    }

    private int brightnessForHeight2(int color, int height, int north, int south) {
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
