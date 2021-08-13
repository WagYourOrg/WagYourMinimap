package xyz.wagyourtail.oldminimap.scanner.updater;

import net.minecraft.world.level.Level;
import xyz.wagyourtail.LazyResolver;
import xyz.wagyourtail.oldminimap.WagYourMinimap;
import xyz.wagyourtail.oldminimap.api.MinimapEvents;
import xyz.wagyourtail.oldminimap.client.WagYourMinimapClient;
import xyz.wagyourtail.oldminimap.client.gui.image.AbstractImageStrategy;
import xyz.wagyourtail.oldminimap.scanner.ChunkData;
import xyz.wagyourtail.oldminimap.scanner.MapLevel;
import xyz.wagyourtail.oldminimap.scanner.MapRegion;

import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;

public abstract class AbstractChunkUpdateStrategy {

    public AbstractChunkUpdateStrategy() {
        registerEventListener();
    }

    protected LazyResolver<ChunkData> updateChunk(String server_slug, String level_slug, Level level, MapLevel.Pos regionPos, int chunkIndex, BiFunction<MapRegion, ChunkData, ChunkData> newChunkDataCreator) {
        MapLevel currentLevel = WagYourMinimap.INSTANCE.currentLevel;
        if ((currentLevel == null) || (!currentLevel.server_slug.equals(server_slug) || !currentLevel.level_slug.equals(level_slug))) {
            if (currentLevel != null) {
                currentLevel.close();
            }
            WagYourMinimap.INSTANCE.currentLevel = currentLevel = new MapLevel(server_slug, level_slug, level.getMinBuildHeight(), level.getMaxBuildHeight());
        }
        try {
            MapRegion region = currentLevel.getRegion(regionPos);
            synchronized (region.data) {
                LazyResolver<ChunkData> oldData = region.data[chunkIndex] == null ? new LazyResolver<>(() -> null) : region.data[chunkIndex];
                region.data[chunkIndex] = new LazyResolver<>(() -> {
                    ChunkData newData = newChunkDataCreator.apply(region, oldData.resolve());
                    if (WagYourMinimap.INSTANCE instanceof WagYourMinimapClient inst) {
                        for (AbstractImageStrategy renderLayer : inst.inGameHud.renderer.getRenderLayers()) {
                            if (oldData.resolve() != null)
                                synchronized (oldData.resolve()) {
                                    renderLayer.invalidateChunk(oldData.resolve());
                                }
                        }
                    }
                    return newData;
                }, oldData);
                MinimapEvents.CHUNK_UPDATED.invoker().onChunkUpdated(region, chunkIndex, region.data[chunkIndex], oldData, this.getClass());
                return region.data[chunkIndex];
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected abstract void registerEventListener();
}
