package xyz.wagyourtail.minimap.map;

import net.minecraft.world.level.Level;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.map.chunkdata.cache.AbstractCacher;
import xyz.wagyourtail.minimap.waypoint.WaypointManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MapServer implements AutoCloseable {
    private static final ThreadPoolExecutor save_pool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.NANOSECONDS, new LinkedBlockingQueue<>());
    private final Map<String, MapLevel> levels = new HashMap<>();
    public final String server_slug;
    public final WaypointManager waypoints = new WaypointManager(this);

    public MapServer(String server_slug) {
        this.server_slug = server_slug;
    }

    public static ChunkData loadChunk(ChunkLocation location) {
        for (AbstractCacher cacher : MinimapApi.getInstance().getCachers()) {
            ChunkData data = cacher.loadChunk(location);
            if (data != null) return data;
        }
        return null;
    }

    public static void saveChunk(ChunkLocation location, ChunkData data) {
        MinimapApi.saving.incrementAndGet();
        save_pool.execute(() -> {
            innerRemove(location, data);
            MinimapApi.saving.decrementAndGet();
        });
    }

    private static void innerRemove(ChunkLocation location, ChunkData data) {
        if (data != null) {
            synchronized (data) {
                for (AbstractCacher cacher : MinimapApi.getInstance().getCachers()) {
                    cacher.saveChunk(location, data);
                }
            }
        }
    }


    private static String getLevelName(Level level) {
        return level.dimension().location().toString().replace(":", "_");
    }

    public synchronized MapLevel getLevel(Level level) {
        //TODO: test and figure out how to deal with on multiverse/waterfall servers
        String level_slug = getLevelName(level);
        return levels.computeIfAbsent(level_slug, (slug) -> new MapLevel(this, level_slug, level.getMinBuildHeight(), level.getMaxBuildHeight()));
    }

    @Override
    public synchronized void close() {
        for (MapLevel value : levels.values()) {
            value.close();
        }
        levels.clear();
        waypoints.close();
    }

}
