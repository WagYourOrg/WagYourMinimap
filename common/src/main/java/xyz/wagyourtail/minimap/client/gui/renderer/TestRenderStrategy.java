package xyz.wagyourtail.minimap.client.gui.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import xyz.wagyourtail.minimap.scanner.ChunkData;

import java.awt.*;

public class TestRenderStrategy extends AbstractRenderStrategy {

    public static int getMainTopColor(ResourceLocation block) {
        return Registry.BLOCK.getOptional(block).get().defaultBlockState().getMapColor(Minecraft.getInstance().level, BlockPos.ZERO).col;

    }

    private int colorFormatSwap(int color) {
        return color & 0xFF00FF00 | (color & 0xFF) << 0x10 | color >> 0x10 & 0xFF;
    }

    private int brightnessForHeight(int color, float height) {
        float[] hsb = Color.RGBtoHSB((color & 0xFF0000) >> 0x10, (color & 0xFF00) >> 0x8, color & 0xFF, null);
        //brightness scaled by .75 - 1.0 based on height
        hsb[2] *= .75F + height * .25F;
        return color & 0xFF000000 | Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
    }


    @Override
    public DynamicTexture load(ChunkData key) {
        NativeImage image = new NativeImage(16, 16, false);
        int min = key.parent.parent.minHeight;
        int max = key.parent.parent.maxHeight;
        int height = max - min;
        for (int i = 0; i < 256; ++i) {
            int x = (i >> 4) % 16;
            int z = i % 16;
            int color = 0xFF000000 | getMainTopColor(key.resources.get(key.blockid[i]));
            color = brightnessForHeight(color, (key.heightmap[i] - min) / (float) height);
            image.setPixelRGBA(x, z, colorFormatSwap(color));
        }
        return new DynamicTexture(image);
    }

}
