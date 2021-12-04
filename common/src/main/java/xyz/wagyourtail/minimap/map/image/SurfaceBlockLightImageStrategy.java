package xyz.wagyourtail.minimap.map.image;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.world.level.Level;
import xyz.wagyourtail.minimap.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.chunkdata.parts.SurfaceDataPart;

import java.awt.*;

public record SurfaceBlockLightImageStrategy(boolean lightOverlayInNether) implements ImageStrategy {
    private static final Minecraft minecraft = Minecraft.getInstance();
    private static final int TICKS_PER_DAY = 24000;
    private static final float HUE = 50F / 360F;

    @Override
    public DynamicTexture load(ChunkLocation location, ChunkData key) {
        SurfaceDataPart surface = key.getData(SurfaceDataPart.class).orElse(null);
        if (surface == null) {
            return null;
        }
        NativeImage image = new NativeImage(16, 16, false);
        for (int i = 0; i < 256; ++i) {
            int x = (i >> 4) % 16;
            int z = i % 16;
            image.setPixelRGBA(x, z, 0x7FFFFFFF & colorFormatSwap(colorForLightLevel(surface.blocklight[i])));

        }
        return new DynamicTexture(image);
    }

    private int colorForLightLevel(byte lightLevel) {
        //TODO: don't use awt color it's slow
        return Color.HSBtoRGB(HUE, 1F, lightLevel / 15F);
    }

    @Override
    public boolean shouldRender() {
        assert minecraft.level != null;
        if (minecraft.level.dimension().equals(Level.NETHER) && !lightOverlayInNether) {
            return false;
        }
        long time = minecraft.level.getDayTime() % TICKS_PER_DAY;
        return time > TICKS_PER_DAY / 2;
    }

}
