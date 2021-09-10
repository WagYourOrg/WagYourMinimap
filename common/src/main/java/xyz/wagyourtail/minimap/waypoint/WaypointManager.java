package xyz.wagyourtail.minimap.waypoint;

import com.google.common.collect.ImmutableSet;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.map.MapServer;
import xyz.wagyourtail.minimap.map.chunkdata.cache.AbstractCacher;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class WaypointManager {
    private static final Map<Class<Predicate<Waypoint>>, Predicate<Waypoint>> filters = new HashMap<>();
    private static Predicate<Waypoint> compiledFilter = (a) -> true;
    private final MapServer server;
    private final Set<Waypoint> waypointList = new LinkedHashSet<>();
    //if it starts lagging, I'll add computing in parallel...
    private Set<Waypoint> visibleWaypoints = new HashSet<>();

    public WaypointManager(MapServer server) {
        this.server = server;

        for (AbstractCacher cacher : MinimapApi.getInstance().getCachers()) {
            waypointList.addAll(cacher.loadWaypoints(server));
        }
//        //test waypoint
//        if (waypointList.isEmpty()) {
//            waypointList.add(new Waypoint(0, 0, 0, (byte) 255, (byte) 0, (byte) 0, "TEST", new String[] {}, new String[] {}));
//        }
    }

    public static void addFilter(Class<? extends Predicate<Waypoint>> filter) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        filters.put((Class<Predicate<Waypoint>>) filter, filter.getConstructor().newInstance());
        compileFilter();
    }

    private static void compileFilter() {
        Iterator<Predicate<Waypoint>> iterator = filters.values().iterator();
        if (!iterator.hasNext()) {
            compiledFilter = (a) -> true;
            return;
        }
        Predicate<Waypoint> workingFilter = iterator.next();
        while (iterator.hasNext()) workingFilter = workingFilter.and(iterator.next());
        compiledFilter = workingFilter;
    }

    public static void removeFilter(Class<? extends Predicate<Waypoint>> filter) {
        filters.remove(filter);
        compileFilter();
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
                for (AbstractCacher cacher : MinimapApi.getInstance().getCachers()) {
                    cacher.saveWaypoints(server, waypointList);
                }

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
