package xyz.wagyourtail.minimap.waypoint;

import com.google.common.collect.ImmutableSet;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.api.MinimapEvents;
import xyz.wagyourtail.minimap.map.MapServer;
import xyz.wagyourtail.minimap.waypoint.filters.DimensionFilter;
import xyz.wagyourtail.minimap.waypoint.filters.DistanceFilter;
import xyz.wagyourtail.minimap.waypoint.filters.EnabledFilter;
import xyz.wagyourtail.minimap.waypoint.filters.WaypointFilter;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class WaypointManager {
    private static final Set<WaypointFilter> filters = new HashSet<>(Set.of(
        new EnabledFilter(),
        new DimensionFilter(),
        new DistanceFilter(1000)
    ));
    private static Predicate<Waypoint> compiledFilter;

    static {
        compileFilter();
    }

    private final MapServer server;
    private final Set<Waypoint> waypointList = new LinkedHashSet<>();

    public WaypointManager(MapServer server) {
        this.server = server;

        waypointList.addAll(MinimapApi.getInstance().cacheManager.loadWaypoints(server));
        //        //test waypoint
        //        if (waypointList.isEmpty()) {
        //            waypointList.add(new Waypoint(0, 0, 0, (byte) 255, (byte) 0, (byte) 0, "TEST", new String[] {}, new String[] {}));
        //        }
    }

    @SafeVarargs
    public static void addFilter(Class<? extends WaypointFilter>... filter) {
        filters.addAll(Arrays.stream(filter).map(e -> {
            try {
                return e.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                ex.printStackTrace();
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList()));
        compileFilter();
    }

    private static void compileFilter() {
        Iterator<WaypointFilter> iterator = filters.iterator();
        if (!iterator.hasNext()) {
            compiledFilter = (a) -> true;
            return;
        }
        Predicate<Waypoint> workingFilter = iterator.next();
        while (iterator.hasNext()) {
            workingFilter = workingFilter.and(iterator.next());
        }
        compiledFilter = workingFilter;
    }

    public static void addFilter(WaypointFilter... filter) {
        filters.addAll(Arrays.asList(filter));
        compileFilter();
    }

    public static void removeFilter(Class<? extends Predicate<Waypoint>> filter) {
        filters.removeIf(e -> e.getClass().equals(filter));
        compileFilter();
    }

    public static void removeFilter(Predicate<Waypoint> filter) {
        filters.remove(filter);
        compileFilter();
    }

    public static void clearFilters(boolean compileChange) {
        filters.clear();
        if (compileChange) {
            compileFilter();
        }
    }


    public Set<Waypoint> getAllWaypoints() {
        return ImmutableSet.copyOf(waypointList);
    }

    public synchronized void addWaypoint(Waypoint waypoint) {
        MinimapEvents.WAYPOINT_ADDED.invoker().onWaypoint(waypoint);
        waypointList.add(waypoint);
        saveWaypoints();
    }

    public void saveWaypoints() {
        MapServer.addToSaveQueue(() -> {
            synchronized (this) {
                MinimapApi.getInstance().cacheManager.saveWaypoints(
                    server,
                    waypointList.stream().filter((e) -> !e.ephemeral)
                );
            }
        });
    }

    public synchronized void removeWaypoint(Waypoint waypoint) {
        MinimapEvents.WAYPOINT_REMOVED.invoker().onWaypoint(waypoint);
        waypointList.remove(waypoint);
        saveWaypoints();
    }

    public synchronized void updateWaypoint(Waypoint old, Waypoint newWaypoint) {
        MinimapEvents.WAYPOINT_UPDATED.invoker().onWaypoint(old, newWaypoint);
        waypointList.remove(old);
        waypointList.add(newWaypoint);
        saveWaypoints();
    }

    public Set<Waypoint> getVisibleWaypoints() {
        return waypointList.parallelStream().filter(compiledFilter).collect(Collectors.toSet());
    }

}
