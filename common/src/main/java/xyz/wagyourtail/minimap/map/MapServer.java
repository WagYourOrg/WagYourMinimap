package xyz.wagyourtail.minimap.map;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.waypoint.WaypointManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MapServer {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final ThreadPoolExecutor save_pool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.NANOSECONDS, new LinkedBlockingQueue<>());
    private final Map<String, MapLevel> levels = new HashMap<>();
    public final String server_slug;
    public final WaypointManager waypoints;
    public LevelSupplier currentLevelNameSupplier = new VanillaLevelSupplier();

    public MapServer(String server_slug) {
        this.server_slug = server_slug;
        this.waypoints = new WaypointManager(this);
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

    public MapLevel getCurrentLevel() {
        assert mc.level != null;
        return getLevelFor(currentLevelNameSupplier.get(), mc.level.dimensionType());
    }

    public synchronized MapLevel getLevelFor(String name,  DimensionType dimType) {
        return levels.computeIfAbsent(name, (slug) -> new MapLevel(this, slug, dimType.minY(), dimType.minY() + dimType.height()));
    }

    @Override
    public String toString() {
        return "MapServer{" +
            "server_slug='" + server_slug + '\'' +
            '}';
    }

    public static record MapLevel(MapServer parent, String level_slug, int minHeight, int maxHeight) {}

}
