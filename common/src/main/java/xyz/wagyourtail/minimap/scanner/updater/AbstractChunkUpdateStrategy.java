package xyz.wagyourtail.minimap.scanner.updater;

import xyz.wagyourtail.minimap.WagYourMinimap;
import xyz.wagyourtail.minimap.api.MinimapEvents;
import xyz.wagyourtail.minimap.scanner.ChunkData;
import xyz.wagyourtail.minimap.scanner.MapLevel;
import xyz.wagyourtail.minimap.scanner.MapRegion;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;

public abstract class AbstractChunkUpdateStrategy {
    protected static final WagYourMinimap<?> INSTANCE = WagYourMinimap.INSTANCE;

    public AbstractChunkUpdateStrategy() {
        registerEventListener();
    }

    public boolean overrideIfLocked() {
        return true;
    }

    protected void updateChunk(String server_slug, String level_slug, MapLevel.Pos regionPos, int chunkIndex, BiFunction<MapRegion, ChunkData, ChunkData> newChunkDataCreator) {
        MapLevel currentLevel = INSTANCE.currentLevel;
        if (!currentLevel.server_slug.equals(server_slug) || !currentLevel.level_slug.equals(level_slug)) INSTANCE.currentLevel = currentLevel = new MapLevel(server_slug, level_slug);
        MapLevel finalCurrentLevel = currentLevel;
        CompletableFuture.runAsync(() -> {
            try {
                MapRegion region = finalCurrentLevel.getRegion(regionPos);
                synchronized (region.datalocks) {
                    if (region.datalocks[chunkIndex] && !overrideIfLocked()) return;
                    while (region.datalocks[chunkIndex]) region.datalocks.wait();
                    region.datalocks[chunkIndex] = true;
                }
                region.data[chunkIndex] = newChunkDataCreator.apply(region, region.data[chunkIndex]);
                MinimapEvents.CHUNK_UPDATED.invoker().onChunkUpdated(region.data[chunkIndex], this.getClass());
                synchronized (region.datalocks) {
                    region.datalocks[chunkIndex] = false;
                    region.datalocks.notifyAll();
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    protected abstract void registerEventListener();
}
