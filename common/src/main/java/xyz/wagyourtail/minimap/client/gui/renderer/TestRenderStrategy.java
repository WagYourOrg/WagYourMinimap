package xyz.wagyourtail.minimap.client.gui.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import xyz.wagyourtail.minimap.scanner.ChunkData;

public class TestRenderStrategy extends AbstractRenderStrategy {

    private int getMainTopColor(ResourceLocation block) {
        int c = Registry.BLOCK.getOptional(block).get().defaultBlockState().getMapColor(null, null).col;
        return 0xFF000000 + ((c & 0xFF0000) >> 16) + (c & 0xFF00) + (c & 0xFF << 16);
    }


    @Override
    public DynamicTexture load(ChunkData key) {
        NativeImage image = new NativeImage(16, 16, false);
        int min = key.parent.parent.minHeight;
        int max = key.parent.parent.maxHeight;
        int height = max - min;
        int resources = key.resources.size();
        for (int i = 0; i < 256; ++i) {
            int x = (i >> 4) % 16;
            int z = i % 16;
            int abgr = getMainTopColor(key.resources.get(key.blockid[i]));
            image.setPixelRGBA(x, z, abgr);
        }
        return new DynamicTexture(image);
    }

}
