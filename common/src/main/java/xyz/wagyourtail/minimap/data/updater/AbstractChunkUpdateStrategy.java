package xyz.wagyourtail.minimap.data.updater;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import xyz.wagyourtail.ResolveQueue;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.api.MinimapEvents;
import xyz.wagyourtail.minimap.data.ChunkData;
import xyz.wagyourtail.minimap.data.ChunkLocation;
import xyz.wagyourtail.minimap.data.MapLevel;

import java.util.function.BiFunction;

public abstract class AbstractChunkUpdateStrategy {
    private final int priority;
    public AbstractChunkUpdateStrategy(int priority) {
        registerEventListener();
        this.priority = priority;
    }

    protected void updateChunk(ChunkLocation location, BiFunction<ChunkLocation, ChunkData, ChunkData> newChunkDataCreator) {
        synchronized (location.level()) {
            ResolveQueue<ChunkData> chunkData = location.level().getChunk(location);
            chunkData.addTask((od) -> newChunkDataCreator.apply(location, od), priority);
            MinimapEvents.CHUNK_UPDATED.invoker().onChunkUpdated(location, chunkData, this.getClass());
        }
    }

    protected ChunkLocation getChunkLocation(Level level, int chunkX, int chunkZ) {
        return ChunkLocation.locationForChunkPos(updateCurrentLevel(level), chunkX, chunkZ);
    }

    private MapLevel updateCurrentLevel(Level level) {
        MapLevel currentLevel = MinimapApi.getInstance().getCurrentLevel();
        String server_slug = MinimapApi.getInstance().getServerName();
        String level_slug = MinimapApi.getInstance().getLevelName(level);
        if ((currentLevel == null) || (!currentLevel.server_slug.equals(server_slug) || !currentLevel.level_slug.equals(level_slug))) {
            MinimapApi.getInstance().setCurrentLevel(currentLevel = new MapLevel(server_slug, level_slug, level.getMinBuildHeight(), level.getMaxBuildHeight()));
        }
        return currentLevel;
    }

    protected ChunkLocation getChunkLocation(Level level, ChunkPos pos) {
        return ChunkLocation.locationForChunkPos(updateCurrentLevel(level), pos);
    }

    protected abstract void registerEventListener();
}
