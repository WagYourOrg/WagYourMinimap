package xyz.wagyourtail.minimap.client.gui.image;

import net.minecraft.client.Minecraft;
import xyz.wagyourtail.minimap.client.gui.ThreadsafeDynamicTexture;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkLocation;

import java.util.concurrent.ExecutionException;

public abstract class AbstractImageStrategy {
    protected static final Minecraft minecraft = Minecraft.getInstance();

    public static int colorFormatSwap(int color) {
        return color & 0xFF00FF00 | (color & 0xFF) << 0x10 | color >> 0x10 & 0xFF;
    }

    public synchronized ThreadsafeDynamicTexture getImage(ChunkLocation key) throws ExecutionException {
        ChunkData data = key.level().getChunk(key);
        if (data != null) {
            return data.computeDerivitive(this.getClass().getCanonicalName(), () -> this.load(key, data));
        }
        return null;
    }

    public abstract ThreadsafeDynamicTexture load(ChunkLocation location, ChunkData data);

    public boolean shouldRender() {
        return true;
    }

}
