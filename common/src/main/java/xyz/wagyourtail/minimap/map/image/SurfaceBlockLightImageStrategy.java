package xyz.wagyourtail.minimap.map.image;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.world.level.Level;
import xyz.wagyourtail.config.field.Setting;
import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.chunkdata.parts.LightDataPart;
import xyz.wagyourtail.minimap.chunkdata.parts.SurfaceDataPart;

@SettingsContainer("gui.wagyourminimap.setting.layers.light")
public class SurfaceBlockLightImageStrategy implements ImageStrategy {
    private static final Minecraft minecraft = Minecraft.getInstance();
    private static final int TICKS_PER_DAY = 24000;
    private static final float HUE = 50F / 360F;
    private static final int[] HSB_TO_RGB_PRECACHE = new int[0x10];

    static {
        for (int i = 0; i < 16; i++) {
            HSB_TO_RGB_PRECACHE[i] = SurfaceBlockLightImageStrategy.HSBtoRGB2(HUE, 1F, i / 15F);
        }
    }

    @Setting(value = "gui.wagyourminimap.setting.layers.light.nether")
    public boolean nether = false;

    public static int HSBtoRGB2(float h, float s, float b) {
        return 0xFF000000 | (f(h, s, b, 5) << 16) | (f(h, s, b, 3) << 8) | f(h, s, b, 1);
    }

    private static int f(float h, float s, float b, float n) {
        float v = (n + h * 6) % 6;
        return (int) (b * (1 - s * Math.max(0, Math.min(Math.min(v, 4 - v), 1))) * 255);
    }

    @Override
    public DynamicTexture load(ChunkLocation location, ChunkData key) {
        LightDataPart surface = key.getData(LightDataPart.class).orElse(null);
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
        return HSB_TO_RGB_PRECACHE[lightLevel & 0xF];
    }

    @Override
    public boolean shouldRender() {
        assert minecraft.level != null;
        if (minecraft.level.dimension().equals(Level.NETHER) && !nether) {
            return false;
        }
        long time = minecraft.level.getDayTime() % TICKS_PER_DAY;
        return time > TICKS_PER_DAY / 2;
    }

}
