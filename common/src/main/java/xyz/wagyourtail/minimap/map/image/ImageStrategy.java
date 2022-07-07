package xyz.wagyourtail.minimap.map.image;

import net.minecraft.client.renderer.texture.DynamicTexture;
import xyz.wagyourtail.minimap.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.chunkdata.ChunkLocation;

import java.util.concurrent.ExecutionException;

public interface ImageStrategy {
    default int colorFormatSwap(int color) {
        return color & 0xFF00FF00 | (color & 0xFF) << 0x10 | color >> 0x10 & 0xFF;
    }

    default DynamicTexture getImage(ChunkLocation key) throws ExecutionException {
        synchronized (this) {
            ChunkData data = key.get();
            return data.computeDerivative(getDerivitiveKey(), () -> this.load(key, data));
        }
    }

    default String getDerivitiveKey() {
        return this.getClass().getCanonicalName();
    }

    DynamicTexture load(ChunkLocation location, ChunkData data);

    default boolean shouldRender() {
        return true;
    }

}
