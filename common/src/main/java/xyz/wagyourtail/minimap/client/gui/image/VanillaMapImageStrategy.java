package xyz.wagyourtail.minimap.client.gui.image;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import xyz.wagyourtail.LazyResolver;
import xyz.wagyourtail.minimap.client.gui.ThreadsafeDynamicTexture;
import xyz.wagyourtail.minimap.scanner.ChunkData;

import java.awt.*;
import java.util.concurrent.ExecutionException;

public class VanillaMapImageStrategy extends AbstractImageStrategy {
    private final static ResourceLocation water = Registry.BLOCK.getKey(Blocks.WATER);


    public static int getBlockColor(ResourceLocation block) {
        return Registry.BLOCK.getOptional(block).get().defaultBlockState().getMapColor(Minecraft.getInstance().level, BlockPos.ZERO).col;

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

    private int brightnessForHeight(int color, float height) {
        float[] hsb = Color.RGBtoHSB((color & 0xFF0000) >> 0x10, (color & 0xFF00) >> 0x8, color & 0xFF, null);
        //brightness scaled by .75 - 1.0 based on height
        hsb[2] *= .75F + height * .25F;
        return color & 0xFF000000 | Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
    }


    @Override
    public LazyResolver<ThreadsafeDynamicTexture> load(ChunkLocation key) {
        return new LazyResolver<>(() -> {
            LazyResolver<ChunkData> ldata;
            ChunkData data;
            try {
                ldata = key.level().getRegion(key.region()).data[key.index()];
                if (ldata == null) return null;
                data = ldata.resolve();
                if (data == null) return null;
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
            NativeImage image = new NativeImage(16, 16, false);
            int min = data.parent.parent.minHeight;
            int max = data.parent.parent.maxHeight;
            int height = max - min;
            for (int i = 0; i < 256; ++i) {
                int x = (i >> 4) % 16;
                int z = i % 16;
                ResourceLocation currentBlock = data.resources.get(data.blockid[i]);
                int color;
                if (currentBlock.equals(water)) {
                    float waterRatio = Math.min(
                        // 0.8 - 1.0 depending on depth of water 1 - 10 blocks...
                        .8F + .2F * (data.heightmap[i] - data.oceanFloorHeightmap[i]) / 10F,
                        1.0F
                    );
                    color = colorCombine(
                        getBlockColor(currentBlock),
                        getBlockColor(data.resources.get(data.oceanFloorBlockid[i])),
                        waterRatio
                    );
                } else {
                    color = 0xFF000000 | getBlockColor(currentBlock);
                }
                color = brightnessForHeight(color, (data.heightmap[i] - min) / (float) height);
                image.setPixelRGBA(x, z, colorFormatSwap(color));
            }
            return new ThreadsafeDynamicTexture(image);
        });
    }

}
