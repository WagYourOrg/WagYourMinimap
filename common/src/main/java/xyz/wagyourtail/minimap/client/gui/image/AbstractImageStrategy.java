package xyz.wagyourtail.minimap.client.gui.image;

import net.minecraft.client.Minecraft;
import xyz.wagyourtail.ResolveQueue;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.client.gui.ThreadsafeDynamicTexture;

import java.util.concurrent.ExecutionException;

public abstract class AbstractImageStrategy {
    protected static final Minecraft minecraft = Minecraft.getInstance();

    public static int colorFormatSwap(int color) {
        return color & 0xFF00FF00 | (color & 0xFF) << 0x10 | color >> 0x10 & 0xFF;
    }

    public synchronized ResolveQueue<ThreadsafeDynamicTexture> getImage(ChunkLocation key) throws ExecutionException {
        ResolveQueue<ChunkData> data = key.level().getChunk(key);
        if (data != null) {
            ChunkData resolved = data.getNow();
            if (resolved != null) {
                return resolved.computeDerivitive(this.getClass().getCanonicalName(), () -> this.load(key, resolved));
            }
        }
        return new ResolveQueue<>(null, null);
    }

    public abstract ThreadsafeDynamicTexture load(ChunkLocation location, ChunkData data);

    public boolean shouldRender() {
        return true;
    }

}
