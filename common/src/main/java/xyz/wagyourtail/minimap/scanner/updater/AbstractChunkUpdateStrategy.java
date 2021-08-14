package xyz.wagyourtail.minimap.scanner.updater;

import net.minecraft.world.level.Level;
import xyz.wagyourtail.LazyResolver;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.api.MinimapEvents;
import xyz.wagyourtail.minimap.scanner.ChunkData;
import xyz.wagyourtail.minimap.scanner.MapLevel;
import xyz.wagyourtail.minimap.scanner.MapRegion;

import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;

public abstract class AbstractChunkUpdateStrategy {

    public AbstractChunkUpdateStrategy() {
        registerEventListener();
    }

    protected void updateChunk(String server_slug, String level_slug, Level level, MapLevel.Pos regionPos, int chunkIndex, BiFunction<MapRegion, ChunkData, ChunkData> newChunkDataCreator) {
        MapLevel currentLevel = MinimapApi.getInstance().getCurrentLevel();
        if ((currentLevel == null) || (!currentLevel.server_slug.equals(server_slug) || !currentLevel.level_slug.equals(level_slug))) {
            MinimapApi.getInstance().setCurrentLevel(currentLevel = new MapLevel(server_slug, level_slug, level.getMinBuildHeight(), level.getMaxBuildHeight()));
        }
        try {
            MapRegion region = currentLevel.getRegion(regionPos);
            LazyResolver<ChunkData> newResolver;
            synchronized (region) {
                LazyResolver<ChunkData> oldData = region.getChunk(chunkIndex);
                if (oldData == null) {
                    region.setChunkData(chunkIndex, newResolver = new LazyResolver<>(() -> newChunkDataCreator.apply(region, null)));
                } else {
                    region.setChunkData(chunkIndex, newResolver = oldData.then(od -> newChunkDataCreator.apply(region, od), true));
                }
                MinimapEvents.CHUNK_UPDATED.invoker().onChunkUpdated(region, chunkIndex, newResolver, oldData, this.getClass());
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    protected abstract void registerEventListener();
}
