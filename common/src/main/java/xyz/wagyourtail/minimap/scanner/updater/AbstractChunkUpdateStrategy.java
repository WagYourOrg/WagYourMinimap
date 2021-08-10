package xyz.wagyourtail.minimap.scanner.updater;

import net.minecraft.world.level.Level;
import xyz.wagyourtail.minimap.WagYourMinimap;
import xyz.wagyourtail.minimap.api.MinimapEvents;
import xyz.wagyourtail.minimap.client.WagYourMinimapClient;
import xyz.wagyourtail.minimap.scanner.ChunkData;
import xyz.wagyourtail.minimap.scanner.MapLevel;
import xyz.wagyourtail.minimap.scanner.MapRegion;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiFunction;

public abstract class AbstractChunkUpdateStrategy {

    public final ForkJoinPool strategyAsyncPool = new ForkJoinPool();

    public AbstractChunkUpdateStrategy() {
        registerEventListener();
    }

    public boolean overrideIfLocked() {
        return true;
    }

    protected void updateChunk(String server_slug, String level_slug, Level level, MapLevel.Pos regionPos, int chunkIndex, BiFunction<MapRegion, ChunkData, ChunkData> newChunkDataCreator) {
        MapLevel currentLevel = WagYourMinimap.INSTANCE.currentLevel;
        if ((currentLevel == null) || (!currentLevel.server_slug.equals(server_slug) || !currentLevel.level_slug.equals(level_slug))) {
            if (WagYourMinimap.INSTANCE.currentLevel != null) {
                WagYourMinimap.INSTANCE.currentLevel.close();
            }
            WagYourMinimap.INSTANCE.currentLevel = currentLevel = new MapLevel(server_slug, level_slug, level.getMinBuildHeight(), level.getMaxBuildHeight());
        }
        MapLevel finalCurrentLevel = currentLevel;
        CompletableFuture.runAsync(() -> {
            try {
                MapRegion region = finalCurrentLevel.getRegion(regionPos);
                synchronized (region.datalocks) {
                    if (region.datalocks[chunkIndex] && !overrideIfLocked()) return;
                    while (region.datalocks[chunkIndex]) region.datalocks.wait();
                    region.datalocks[chunkIndex] = true;
                }
                ChunkData oldData = region.data[chunkIndex];
                region.data[chunkIndex] = newChunkDataCreator.apply(region, region.data[chunkIndex]);
                MinimapEvents.CHUNK_UPDATED.invoker().onChunkUpdated(region.data[chunkIndex], oldData, this.getClass());
                if (WagYourMinimap.INSTANCE instanceof WagYourMinimapClient inst) {
                    inst.inGameHud.getRenderer().invalidateChunk(oldData);
                }
                synchronized (region.datalocks) {
                    region.datalocks[chunkIndex] = false;
                    region.datalocks.notifyAll();
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, strategyAsyncPool);
    }

    protected abstract void registerEventListener();
}
