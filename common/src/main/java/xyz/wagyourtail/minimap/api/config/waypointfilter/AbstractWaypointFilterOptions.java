package xyz.wagyourtail.minimap.api.config.waypointfilter;

import xyz.wagyourtail.minimap.waypoint.filters.WaypointFilter;

public abstract class AbstractWaypointFilterOptions<T extends WaypointFilter> {

    public abstract T compileFilter();
}
