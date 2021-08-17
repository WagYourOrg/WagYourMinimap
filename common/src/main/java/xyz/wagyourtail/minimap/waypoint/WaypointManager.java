package xyz.wagyourtail.minimap.waypoint;

import xyz.wagyourtail.minimap.map.MapServer;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class WaypointManager implements AutoCloseable {
    private static final Map<Class<Predicate<Waypoint>>, Predicate<Waypoint>> filters = new HashMap<>();
    private static Predicate<Waypoint> compiledFilter = (a) -> true;
    private final MapServer server;
    private final List<Waypoint> waypointList = new ArrayList<>();
    //if it starts lagging, I'll add computing in parallel...
    private List<Waypoint> visibleWaypoints = new ArrayList<>();

    public WaypointManager(MapServer server) {
        this.server = server;
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

    public List<Waypoint> getVisibleWaypoints() {
        return visibleWaypoints = waypointList.parallelStream().filter(compiledFilter).collect(Collectors.toList());
    }

    @Override
    public void close() {

    }

}
