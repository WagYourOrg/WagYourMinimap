package xyz.wagyourtail.minimap.map;

import com.google.common.collect.ImmutableSet;
import net.minecraft.world.level.Level;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.map.chunkdata.cache.AbstractCacher;
import xyz.wagyourtail.minimap.waypoint.WaypointManager;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MapServer implements AutoCloseable {
    private static final AtomicInteger saving = new AtomicInteger(0);
    private static final ThreadPoolExecutor save_pool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.NANOSECONDS, new LinkedBlockingQueue<>());
    private static final Map<Class<? extends AbstractCacher>, AbstractCacher> cachers = new HashMap<>();
    private final Map<String, MapLevel> levels = new HashMap<>();
    public final String server_slug;
    public final WaypointManager waypoints = new WaypointManager(this);

    public MapServer(String server_slug) {
        this.server_slug = server_slug;
    }

    public static void addCacher(Class<? extends AbstractCacher> cacher) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        cachers.put(cacher, cacher.getConstructor().newInstance());
    }

    public static void removeCacher(Class<? extends AbstractCacher> cacher) {
        cachers.remove(cacher);
    }

    public static ChunkData loadChunk(ChunkLocation location) {
        for (AbstractCacher cacher : ImmutableSet.copyOf(MapServer.cachers.values())) {
            ChunkData data = cacher.loadChunk(location);
            if (data != null) return data;
        }
        return null;
    }

    public static void saveChunk(ChunkLocation location, ChunkData data) {
        saving.incrementAndGet();
        save_pool.execute(() -> {
            innerRemove(location, data);
            saving.decrementAndGet();
        });
    }

    private static void innerRemove(ChunkLocation location, ChunkData data) {
        if (data != null) {
            synchronized (data) {
                for (AbstractCacher cacher : ImmutableSet.copyOf(cachers.values())) {
                    cacher.saveChunk(location, data);
                }
            }
        }
    }

    public static int getSaving() {
        return saving.get();
    }

    public synchronized MapLevel getLevel(Level level) {
        //TODO: test and figure out how to deal with on multiverse/waterfall servers
        String level_slug = getLevelName(level);
        return levels.computeIfAbsent(level_slug, (slug) -> new MapLevel(this, level_slug, level.getMinBuildHeight(), level.getMaxBuildHeight()));
    }

    public static String getLevelName(Level level) {
        return level.dimension().location().toString().replace(":", "_");
    }

    @Override
    public synchronized void close() {
        for (MapLevel value : levels.values()) {
            value.close();
        }
        levels.clear();
    }

}
