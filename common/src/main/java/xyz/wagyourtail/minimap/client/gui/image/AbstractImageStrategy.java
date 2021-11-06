package xyz.wagyourtail.minimap.client.gui.image;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkLocation;

import java.util.concurrent.ExecutionException;

public abstract class AbstractImageStrategy {
    protected static final Minecraft minecraft = Minecraft.getInstance();

    public static int colorFormatSwap(int color) {
        return color & 0xFF00FF00 | (color & 0xFF) << 0x10 | color >> 0x10 & 0xFF;
    }

    public synchronized DynamicTexture getImage(ChunkLocation key) throws ExecutionException {
        ChunkData data = key.get();
        return data.computeDerivative(getDerivitiveKey(), () -> this.load(key, data));
    }

    protected abstract DynamicTexture load(ChunkLocation location, ChunkData data);

    public String getDerivitiveKey() {
        return this.getClass().getCanonicalName();
    }

    public boolean shouldRender() {
        return true;
    }

}
