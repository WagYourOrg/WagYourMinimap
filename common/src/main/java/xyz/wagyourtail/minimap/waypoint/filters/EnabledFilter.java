package xyz.wagyourtail.minimap.waypoint.filters;

import xyz.wagyourtail.minimap.waypoint.Waypoint;

public class EnabledFilter extends WaypointFilter {

    @Override
    public boolean test(Waypoint waypoint) {
        return waypoint.enabled;
    }

}
