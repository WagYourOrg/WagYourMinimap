package xyz.wagyourtail.minimap.waypoint;

import com.google.common.collect.ImmutableSet;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.map.MapServer;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class WaypointManager {
    private static final Set<Predicate<Waypoint>> filters = new HashSet<>();
    private static Predicate<Waypoint> compiledFilter = (a) -> true;
    private final MapServer server;
    private final Set<Waypoint> waypointList = new LinkedHashSet<>();
    //if it starts lagging, I'll add computing in parallel...
    private Set<Waypoint> visibleWaypoints = new HashSet<>();

    public WaypointManager(MapServer server) {
        this.server = server;

        waypointList.addAll(MinimapApi.getInstance().cacheManager.loadWaypoints(server));
//        //test waypoint
//        if (waypointList.isEmpty()) {
//            waypointList.add(new Waypoint(0, 0, 0, (byte) 255, (byte) 0, (byte) 0, "TEST", new String[] {}, new String[] {}));
//        }
    }

    @SafeVarargs
    public static void addFilter(Class<? extends Predicate<Waypoint>> ...filter) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
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

    @SafeVarargs
    public static void addFilter(Predicate<Waypoint> ...filter) {
        filters.addAll(Arrays.asList(filter));
        compileFilter();
    }

    private static void compileFilter() {
        Iterator<Predicate<Waypoint>> iterator = filters.iterator();
        if (!iterator.hasNext()) {
            compiledFilter = (a) -> true;
            return;
        }
        Predicate<Waypoint> workingFilter = iterator.next();
        while (iterator.hasNext()) workingFilter = workingFilter.and(iterator.next());
        compiledFilter = workingFilter;
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
        if (compileChange) compileFilter();
    }


    public Set<Waypoint> getAllWaypoints() {
        return ImmutableSet.copyOf(waypointList);
    }

    public synchronized void addWaypoint(Waypoint waypoint) {
        waypointList.add(waypoint);
        saveWaypoints();
    }

    public void saveWaypoints() {
        MapServer.addToSaveQueue(() -> {
            synchronized (this) {
                MinimapApi.getInstance().cacheManager.saveWaypoints(server, waypointList.stream().filter(Predicate.not(Waypoint::ephemeral)));
            }
        });
    }

    public synchronized void removeWaypoint(Waypoint waypoint) {
        waypointList.remove(waypoint);
        saveWaypoints();
    }

    public Set<Waypoint> getVisibleWaypoints() {
        return visibleWaypoints = waypointList.parallelStream().filter(compiledFilter).collect(Collectors.toSet());
    }

}
