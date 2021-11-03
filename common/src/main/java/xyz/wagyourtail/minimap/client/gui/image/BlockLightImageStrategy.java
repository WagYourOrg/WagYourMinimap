package xyz.wagyourtail.minimap.client.gui.image;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import xyz.wagyourtail.minimap.client.gui.ThreadsafeDynamicTexture;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.map.chunkdata.parts.SurfaceDataPart;

import java.awt.*;

public class BlockLightImageStrategy extends AbstractImageStrategy {

    private static final int TICKS_PER_DAY = 24000;
    private static final float HUE = 50F / 360F;

    private boolean lightOverlayInNether;

    public BlockLightImageStrategy(boolean lightOverlayInNether) {
        this.lightOverlayInNether = lightOverlayInNether;
    }

    @Override
    public ThreadsafeDynamicTexture load(ChunkLocation location, ChunkData key) {
        SurfaceDataPart surface = key.getData(SurfaceDataPart.class).orElse(null);
        if (surface == null) return null;
        NativeImage image = new NativeImage(16, 16, false);
        for (int i = 0; i < 256; ++i) {
            int x = (i >> 4) % 16;
            int z = i % 16;
            image.setPixelRGBA(x, z, 0x7FFFFFFF & colorFormatSwap(colorForLightLevel(surface.blocklight[i])));

        }
        return new ThreadsafeDynamicTexture(image);
    }

    private int colorForLightLevel(byte lightLevel) {
        return Color.HSBtoRGB(HUE, 1F, lightLevel / 15F);
    }

    @Override
    public boolean shouldRender() {
        assert minecraft.level != null;
        if (minecraft.level.dimension().equals(Level.NETHER) && !lightOverlayInNether) return false;
        long time = minecraft.level.getDayTime() % TICKS_PER_DAY;
        return time > TICKS_PER_DAY / 2;
    }

}
