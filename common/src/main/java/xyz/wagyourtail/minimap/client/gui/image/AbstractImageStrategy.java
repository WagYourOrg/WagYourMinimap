package xyz.wagyourtail.minimap.client.gui.image;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import xyz.wagyourtail.LazyResolver;
import xyz.wagyourtail.minimap.client.gui.ThreadsafeDynamicTexture;
import xyz.wagyourtail.minimap.scanner.MapLevel;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public abstract class AbstractImageStrategy extends CacheLoader<AbstractImageStrategy.ChunkLocation, LazyResolver<ThreadsafeDynamicTexture>> {

    private final LoadingCache<ChunkLocation, LazyResolver<ThreadsafeDynamicTexture>> imageCache = createCache();

    //wtf, why doesn't newBuilder do this method sig instead of just <Object, Object>
    private <K,V> CacheBuilder<K, V> createCacheBuilder() {
        return (CacheBuilder) CacheBuilder.newBuilder();
    }

    private LoadingCache<ChunkLocation, LazyResolver<ThreadsafeDynamicTexture>> createCache() {
        CacheBuilder<ChunkLocation, LazyResolver<ThreadsafeDynamicTexture>> cache = createCacheBuilder()
            .expireAfterAccess(60000, TimeUnit.MILLISECONDS)
            .removalListener(this::onChunkRemoval);

        return cache.build(this);
    }

    public synchronized void invalidateChunk(ChunkLocation chunkData) {
        imageCache.invalidate(chunkData);
    }

    public void invalidateAll() {
        imageCache.invalidateAll();
    }

    public void onChunkRemoval(RemovalNotification<ChunkLocation, LazyResolver<ThreadsafeDynamicTexture>> notification) {
//        RenderSystem.recordRenderCall(() -> {
            synchronized (notification.getKey()) {
                notification.getValue().close();
            }
//        });
    }

    public synchronized LazyResolver<ThreadsafeDynamicTexture> getImage(ChunkLocation chunk) throws ExecutionException {
        return imageCache.get(chunk);
    }

    public boolean shouldRender() {
        return true;
    }

    public static int colorFormatSwap(int color) {
        return color & 0xFF00FF00 | (color & 0xFF) << 0x10 | color >> 0x10 & 0xFF;
    }
    public record ChunkLocation(MapLevel level, MapLevel.Pos region, int index) {}
}
