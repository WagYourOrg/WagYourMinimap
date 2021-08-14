package xyz.wagyourtail.minimap.client.gui.image;

import net.minecraft.client.Minecraft;
import xyz.wagyourtail.LazyResolver;
import xyz.wagyourtail.minimap.client.gui.ThreadsafeDynamicTexture;
import xyz.wagyourtail.minimap.scanner.ChunkData;
import xyz.wagyourtail.minimap.scanner.MapLevel;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public abstract class AbstractImageStrategy {
    protected static final Minecraft minecraft = Minecraft.getInstance();

    public synchronized LazyResolver<ThreadsafeDynamicTexture> getImage(ChunkLocation key) throws ExecutionException {
        LazyResolver<ChunkData> data = key.level().getRegion(key.region()).data[key.index()];
        try {
            if (data != null) {
                ChunkData resolved = data.resolveAsync(0);
                if (resolved != null) {
                    return resolved.computeDerivitive(this.getClass().getCanonicalName(), () -> this.load(resolved));
                } else {
                    System.out.print("a");
                }
            }
        } catch (InterruptedException | TimeoutException e) {
            e.printStackTrace();
        }
        return new LazyResolver<>((ThreadsafeDynamicTexture) null);
    }

    public abstract ThreadsafeDynamicTexture load(ChunkData data);

    public boolean shouldRender() {
        return true;
    }

    public static int colorFormatSwap(int color) {
        return color & 0xFF00FF00 | (color & 0xFF) << 0x10 | color >> 0x10 & 0xFF;
    }
    public record ChunkLocation(MapLevel level, MapLevel.Pos region, int index) {}
}
