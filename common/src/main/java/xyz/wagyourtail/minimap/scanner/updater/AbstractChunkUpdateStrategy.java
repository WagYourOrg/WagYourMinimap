package xyz.wagyourtail.minimap.scanner.updater;

import net.minecraft.world.level.Level;
import xyz.wagyourtail.LazyResolver;
import xyz.wagyourtail.minimap.WagYourMinimap;
import xyz.wagyourtail.minimap.api.MinimapEvents;
import xyz.wagyourtail.minimap.client.WagYourMinimapClient;
import xyz.wagyourtail.minimap.client.gui.renderer.AbstractRenderStrategy;
import xyz.wagyourtail.minimap.scanner.ChunkData;
import xyz.wagyourtail.minimap.scanner.MapLevel;
import xyz.wagyourtail.minimap.scanner.MapRegion;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiFunction;

public abstract class AbstractChunkUpdateStrategy {
    private static final ForkJoinPool pool = new ForkJoinPool();

    public AbstractChunkUpdateStrategy() {
        registerEventListener();
    }

    protected void updateChunk(String server_slug, String level_slug, Level level, MapLevel.Pos regionPos, int chunkIndex, BiFunction<MapRegion, ChunkData, ChunkData> newChunkDataCreator) {
        MapLevel currentLevel = WagYourMinimap.INSTANCE.currentLevel;
        if ((currentLevel == null) || (!currentLevel.server_slug.equals(server_slug) || !currentLevel.level_slug.equals(level_slug))) {
            if (WagYourMinimap.INSTANCE.currentLevel != null) {
                WagYourMinimap.INSTANCE.currentLevel.close();
            }
            WagYourMinimap.INSTANCE.currentLevel = currentLevel = new MapLevel(server_slug, level_slug, level.getMinBuildHeight(), level.getMaxBuildHeight());
        }
        try {
            MapRegion region = currentLevel.getRegion(regionPos);
            synchronized (region.data) {
                LazyResolver<ChunkData> oldData = region.data[chunkIndex] == null ? new LazyResolver<>(() -> null) : region.data[chunkIndex];
                region.data[chunkIndex] = new LazyResolver<>(() -> {
                    ChunkData newData = newChunkDataCreator.apply(region, oldData.resolve());
                    MinimapEvents.CHUNK_UPDATED.invoker().onChunkUpdated(newData, oldData.resolve(), this.getClass());
                    if (WagYourMinimap.INSTANCE instanceof WagYourMinimapClient inst) {
                        for (AbstractRenderStrategy renderLayer : inst.inGameHud.getRenderLayers()) {
                            if (oldData.resolve() != null)
                                renderLayer.invalidateChunk(oldData.resolve());
                        }
                    }
                    return newData;
                });
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    protected abstract void registerEventListener();
}
