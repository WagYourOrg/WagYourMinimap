package xyz.wagyourtail.minimap.waypoint.filters;

import xyz.wagyourtail.minimap.waypoint.Waypoint;

import java.util.function.Predicate;

public abstract class WaypointFilter implements Predicate<Waypoint> {

    @Override
    public boolean equals(Object obj) {
        return this.getClass().equals(obj.getClass());
    }

    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }

}
