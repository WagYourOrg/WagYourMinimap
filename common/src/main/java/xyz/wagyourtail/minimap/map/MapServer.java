package xyz.wagyourtail.minimap.map;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.map.chunkdata.cache.AbstractCacher;
import xyz.wagyourtail.minimap.waypoint.WaypointManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MapServer implements AutoCloseable {
    private static final ThreadPoolExecutor save_pool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.NANOSECONDS, new LinkedBlockingQueue<>());
    private final Map<String, MapLevel> levels = new HashMap<>();
    private final Set<String> availableLevels;
    public final String server_slug;
    public final WaypointManager waypoints;

    public MapServer(String server_slug) {
        this.server_slug = server_slug;
        ClientPacketListener packetListener = Minecraft.getInstance().getConnection();
        if (packetListener != null) {
            this.availableLevels = ImmutableSet.copyOf(packetListener.levels().stream().map(MapServer::getLevelName).collect(
                Collectors.toSet()));
        } else {
            this.availableLevels = ImmutableSet.of();
        }
        this.waypoints = new WaypointManager(this);
    }

    public static ChunkData loadChunk(ChunkLocation location) {
        for (AbstractCacher cacher : MinimapApi.getInstance().getCachers()) {
            ChunkData data = cacher.loadChunk(location);
            if (data != null) return data;
        }
        return null;
    }

    public static void addToSaveQueue(Runnable saver) {
        MinimapApi.saving.incrementAndGet();
        save_pool.execute(() -> {
            saver.run();
            MinimapApi.saving.decrementAndGet();
        });
    }

    public static void waitForSaveQueue() throws InterruptedException {
        Semaphore lock = new Semaphore(0);
        save_pool.execute(() -> {
            lock.release();
        });
        save_pool.shutdown();
        lock.acquire();
    }

    public synchronized MapLevel getLevel(Level level) {
        //TODO: test and figure out how to deal with on multiverse/waterfall servers
        String level_slug = getLevelName(level);
        return levels.computeIfAbsent(level_slug, (slug) -> new MapLevel(this, level_slug, level.getMinBuildHeight(), level.getMaxBuildHeight()));
    }

    public synchronized MapLevel getLevel(ResourceKey<Level> dimension, DimensionType dimType) {
        //TODO: test and figure out how to deal with on multiverse/waterfall servers
        String level_slug = getLevelName(dimension);
        return levels.computeIfAbsent(level_slug, (slug) -> new MapLevel(this, level_slug, dimType.minY(), dimType.minY() + dimType.height()));
    }

    public Set<String> getAvailableLevels() {
        return availableLevels;
    }

    public static String getLevelName(Level level) {
        return getLevelName(level.dimension());
    }

    public static String getLevelName(ResourceKey<Level> dimension) {
        return dimension.location().toString().replace(":", "_");
    }

    @Override
    public synchronized void close() {
        for (MapLevel value : levels.values()) {
            value.close();
        }
        levels.clear();
    }


    @Override
    public String toString() {
        return "MapServer{" +
            "server_slug='" + server_slug + '\'' +
            '}';
    }

}
