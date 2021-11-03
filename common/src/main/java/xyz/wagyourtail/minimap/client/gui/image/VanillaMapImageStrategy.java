package xyz.wagyourtail.minimap.client.gui.image;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import xyz.wagyourtail.minimap.client.gui.ThreadsafeDynamicTexture;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.map.chunkdata.parts.NorthHeightmap;
import xyz.wagyourtail.minimap.map.chunkdata.parts.SouthHeightmap;
import xyz.wagyourtail.minimap.map.chunkdata.parts.SurfaceDataPart;

import java.awt.*;
import java.util.Set;

public class VanillaMapImageStrategy extends AbstractImageStrategy {
    public final static Set<ResourceLocation> water = Sets.newHashSet(
        Registry.BLOCK.getKey(Blocks.WATER),
        Registry.BLOCK.getKey(Blocks.SEAGRASS),
        Registry.BLOCK.getKey(Blocks.TALL_SEAGRASS),
        Registry.BLOCK.getKey(Blocks.KELP_PLANT),
        Registry.BLOCK.getKey(Blocks.KELP)
    );

    public final static Set<ResourceLocation> grass = Sets.newHashSet(
        Registry.BLOCK.getKey(Blocks.GRASS_BLOCK),
        Registry.BLOCK.getKey(Blocks.GRASS),
        Registry.BLOCK.getKey(Blocks.TALL_GRASS)
    );

    public final static Set<ResourceLocation> leaves = Sets.newHashSet(
        Registry.BLOCK.getKey(Blocks.ACACIA_LEAVES),
        Registry.BLOCK.getKey(Blocks.AZALEA_LEAVES),
        Registry.BLOCK.getKey(Blocks.BIRCH_LEAVES),
        Registry.BLOCK.getKey(Blocks.DARK_OAK_LEAVES),
        Registry.BLOCK.getKey(Blocks.FLOWERING_AZALEA_LEAVES),
        Registry.BLOCK.getKey(Blocks.JUNGLE_LEAVES),
        Registry.BLOCK.getKey(Blocks.OAK_LEAVES),
        Registry.BLOCK.getKey(Blocks.SPRUCE_LEAVES)
    );

    @Override
    public ThreadsafeDynamicTexture load(ChunkLocation location, ChunkData data) {
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
            ResourceLocation currentBlock = data.getResourceLocation(surface.blockid[i]);
            int color;
            if (water.contains(currentBlock)) {
                if (biome == null) continue;
                float waterRatio = Math.min(
                    // 0.7 - 1.0 depending on depth of water 1 - 10 blocks...
                    .7f + .3f * (surface.heightmap[i] - surface.oceanFloorHeightmap[i]) / 10F,
                    1.0F
                );
                color = colorCombine(
                    biome.getWaterColor(),
                    getBlockColor(data.getResourceLocation(surface.oceanFloorBlockid[i])),
                    waterRatio
                );
            } else if (grass.contains(currentBlock)) {
                if (biome == null) continue;
                color = biome.getGrassColor(x, z);
            } else if (leaves.contains(currentBlock)) {
                if (biome == null) continue;
                color = biome.getFoliageColor();
            } else {
                color = 0xFF000000 | getBlockColor(currentBlock);
            }
            if (z == 0) {
                color = brightnessForHeight(color, surface.heightmap[i], north[15 + 16 * x], surface.heightmap[i+1]);
            } else if (z == 15) {
                color = brightnessForHeight(color, surface.heightmap[i], surface.heightmap[i-1], south[16 * x]);
            } else {
                color = brightnessForHeight(color, surface.heightmap[i], surface.heightmap[i-1], surface.heightmap[i+1]);
            }
            image.setPixelRGBA(x, z, colorFormatSwap(color));
        }
        return new ThreadsafeDynamicTexture(image);
    }

    public static int getBlockColor(ResourceLocation block) {
        return Registry.BLOCK.getOptional(block).orElse(Blocks.AIR).defaultBlockState().getMapColor(Minecraft.getInstance().level, BlockPos.ZERO).col;

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

    private int brightnessForHeight(int color, int height, int north, int south) {
        float[] hsb = Color.RGBtoHSB((color & 0xFF0000) >> 0x10, (color & 0xFF00) >> 0x8, color & 0xFF, null);
        if (north > height) {
            hsb[2] *= .8f;
        } else if (south <= height) {
            hsb[2] *= .9f;
        }
        return color & 0xFF000000 | Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
    }

}
