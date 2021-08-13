package xyz.wagyourtail.oldminimap.client.gui.image;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import com.mojang.blaze3d.systems.RenderSystem;
import xyz.wagyourtail.LazyResolver;
import xyz.wagyourtail.oldminimap.client.gui.ThreadsafeDynamicTexture;
import xyz.wagyourtail.oldminimap.scanner.ChunkData;

import java.util.concurrent.ExecutionException;

public abstract class AbstractImageStrategy extends CacheLoader<ChunkData, LazyResolver<ThreadsafeDynamicTexture>> {

    private final LoadingCache<ChunkData, LazyResolver<ThreadsafeDynamicTexture>> imageCache = createCache();

    //wtf, why doesn't newBuilder do this method sig instead of just <Object, Object>
    private <K,V> CacheBuilder<K, V> createCacheBuilder() {
        return (CacheBuilder) CacheBuilder.newBuilder();
    }

    private LoadingCache<ChunkData, LazyResolver<ThreadsafeDynamicTexture>> createCache() {
        CacheBuilder<ChunkData, LazyResolver<ThreadsafeDynamicTexture>> cache = createCacheBuilder()
            .removalListener(this::onChunkRemoval);

        return cache.build(this);
    }

    public void invalidateChunk(ChunkData chunkData) {
        imageCache.invalidate(chunkData);
    }

    public void onChunkRemoval(RemovalNotification<ChunkData, LazyResolver<ThreadsafeDynamicTexture>> notification) {
        RenderSystem.recordRenderCall(() -> {
            synchronized (notification.getKey()) {
                notification.getValue().close();
            }
        });
    }

    public LazyResolver<ThreadsafeDynamicTexture> getImage(ChunkData chunk) throws ExecutionException {
        return imageCache.get(chunk);
    }

    public boolean shouldRender() {
        return true;
    }

    public static int colorFormatSwap(int color) {
        return color & 0xFF00FF00 | (color & 0xFF) << 0x10 | color >> 0x10 & 0xFF;
    }
}
