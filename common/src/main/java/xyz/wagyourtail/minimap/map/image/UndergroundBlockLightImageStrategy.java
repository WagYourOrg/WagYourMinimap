package xyz.wagyourtail.minimap.map.image;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LightLayer;
import xyz.wagyourtail.minimap.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.chunkdata.parts.UndergroundDataPart;
import xyz.wagyourtail.minimap.chunkdata.updater.UndergroundDataUpdater;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class UndergroundBlockLightImageStrategy implements ImageStrategy {
    protected static final Minecraft minecraft = Minecraft.getInstance();
    private static final float HUE = 50F / 360F;
    private final AtomicInteger lastY = new AtomicInteger(0);

    protected final int lightLevel;

    public UndergroundBlockLightImageStrategy(int lightLevel) {
        this.lightLevel = lightLevel;
    }

    @Override
    public DynamicTexture load(ChunkLocation location, ChunkData data) {
        UndergroundDataPart.Data underground = data.getData(UndergroundDataPart.class).map(e -> e.data[lastY.get()]).orElse(null);
        if (underground == null) {
            return null;
        }
        byte[] lightmap = underground.lightmap();
        NativeImage image = new NativeImage(16, 16, false);
        for (int i = 0; i < 256; ++i) {
            int x = (i >> 4) % 16;
            int z = i % 16;
            image.setPixelRGBA(x, z, 0x7FFFFFFF & colorFormatSwap(colorForLightLevel(lightmap[i])));

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
        assert minecraft.player != null;
        int light = minecraft.level.getLightEngine().getLayerListener(LightLayer.SKY).getLightValue(new BlockPos(minecraft.player.getPosition(0)));
        return light < this.lightLevel;
    }

    @Override
    public String getDerivitiveKey() {
        lastY.set(Mth.clamp((minecraft.cameraEntity.getBlockY() - minecraft.level.dimensionType().minY()) / UndergroundDataUpdater.sectionHeight, 0, minecraft.level.dimensionType().height() / UndergroundDataUpdater.sectionHeight));
        return ImageStrategy.super.getDerivitiveKey() + "$" + lastY;
    }

}
