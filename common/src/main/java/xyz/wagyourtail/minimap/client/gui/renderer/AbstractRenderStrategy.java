package xyz.wagyourtail.minimap.client.gui.renderer;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import net.minecraft.client.renderer.texture.DynamicTexture;
import xyz.wagyourtail.minimap.scanner.ChunkData;

import java.util.concurrent.ExecutionException;

public abstract class AbstractRenderStrategy extends CacheLoader<ChunkData, DynamicTexture> {

    private final LoadingCache<ChunkData, DynamicTexture> imageCache = createCache();

    //wtf, why doesn't newBuilder do this method sig instead of just <Object, Object>
    private <K,V> CacheBuilder<K, V> createCacheBuilder() {
        return (CacheBuilder) CacheBuilder.newBuilder();
    }

    private LoadingCache<ChunkData, DynamicTexture> createCache() {
        CacheBuilder<ChunkData, DynamicTexture> cache = createCacheBuilder()
            .weakKeys()
            .removalListener(this::onChunkRemoval);

        return cache.build(this);
    }

    public void invalidateChunk(ChunkData chunkData) {
        imageCache.invalidate(chunkData);
    }

    public void onChunkRemoval(RemovalNotification<ChunkData, DynamicTexture> notification) {
        notification.getValue().close();
    }

    public DynamicTexture getImage(ChunkData chunk) throws ExecutionException {
        return imageCache.get(chunk);
    }

    public boolean shouldRender() {
        return true;
    }

    public static int colorFormatSwap(int color) {
        return color & 0xFF00FF00 | (color & 0xFF) << 0x10 | color >> 0x10 & 0xFF;
    }
}
