package xyz.wagyourtail.minimap.client.gui.image;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.map.chunkdata.parts.SurfaceDataPart;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class VanillaMapImageStrategy extends AbstractImageStrategy {
    public final static Predicate<Block> water = Set.of(
        Blocks.WATER,
        Blocks.SEAGRASS,
        Blocks.TALL_SEAGRASS,
        Blocks.KELP_PLANT,
        Blocks.KELP
    )::contains;

    public final static Predicate<Block> grass = Set.of(
        Blocks.GRASS_BLOCK,
        Blocks.GRASS,
        Blocks.TALL_GRASS
    )::contains;

    public final static Predicate<Block> leaves = (block) -> block instanceof LeavesBlock;

    private static final Map<Biome, Integer> grassCache = new ConcurrentHashMap<>();
    private static final Map<Biome, Integer> foliageCache = new ConcurrentHashMap<>();
    private static final Map<Biome, Integer> waterCache = new ConcurrentHashMap<>();

    @Override
    public DynamicTexture load(ChunkLocation location, ChunkData data) {
        SurfaceDataPart surface = data.getData(SurfaceDataPart.class).orElse(null);
        if (surface == null) return null;
        NativeImage image = new NativeImage(16, 16, false);
        assert minecraft.level != null;
        Registry<Biome> biomeRegistry = minecraft.level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
        int[] north = data.north().get().getData(SurfaceDataPart.class).map(e -> e.heightmap).orElseGet(() -> new int[256]);
        int[] south = data.south().get().getData(SurfaceDataPart.class).map(e -> e.heightmap).orElseGet(() -> new int[256]);
        int i;
        for (i = 0; i < 256; ++i) {
            int x = (i >> 4) % 16;
            int z = i % 16;
            Biome biome = biomeRegistry.get(data.getResourceLocation(surface.biomeid[i]));
            Block block = Registry.BLOCK.getOptional(data.getResourceLocation(surface.blockid[i])).orElse(Blocks.AIR);
            int color;
            if (water.test(block)) {
                int waterColor = biome == null ? getBlockColor(block) : waterCache.computeIfAbsent(biome, Biome::getWaterColor);
                block = Registry.BLOCK.getOptional(data.getResourceLocation(surface.oceanFloorBlockid[i])).orElse(Blocks.WATER);
                float waterRatio = Math.min(
                    // 0.7 - 1.0 depending on depth of water 1 - 10 blocks...
                    .7f + .3F * (surface.heightmap[i] - surface.oceanFloorHeightmap[i]) / 10F,
                    1.0F
                );
                color = colorCombine(
                    waterColor,
                    getBlockColor(block),
                    waterRatio
                );
            } else if (grass.test(block)) {
                if (biome == null) {
                    color = getBlockColor(block);
                } else {
                    color = grassCache.computeIfAbsent(biome, b -> b.getGrassColor(0, 0));
                }
            } else if (leaves.test(block)) {
                color = (biome == null ? getBlockColor(block) : foliageCache.computeIfAbsent(biome, Biome::getFoliageColor));
            } else {
                color = getBlockColor(block);
            }
            if (z == 0) {
                color = brightnessForHeight2(color, surface.heightmap[i], north[15 + 16 * x], surface.heightmap[i+1]);
            } else if (z == 15) {
                color = brightnessForHeight2(color, surface.heightmap[i], surface.heightmap[i-1], south[16 * x]);
            } else {
                color = brightnessForHeight2(color, surface.heightmap[i], surface.heightmap[i-1], surface.heightmap[i+1]);
            }
            image.setPixelRGBA(x, z, 0xFF000000 | colorFormatSwap(color));
        }
        return new DynamicTextureWithWarning(image);
    }

    public static int getBlockColor(Block block) {
        return block.defaultBlockState().getMapColor(Minecraft.getInstance().level, BlockPos.ZERO).col;

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
