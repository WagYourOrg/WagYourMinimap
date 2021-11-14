package xyz.wagyourtail.minimap.waypoint.filters;

import com.google.common.collect.ImmutableSet;
import xyz.wagyourtail.minimap.waypoint.Waypoint;

import java.util.Arrays;
import java.util.Set;

public class GroupFilter extends WaypointFilter {
    private final Set<String> groups;

    public GroupFilter(Set<String> groups) {
        this.groups = ImmutableSet.copyOf(groups);
    }

    @Override
    public boolean test(Waypoint waypoint) {
        return Arrays.stream(waypoint.groups).anyMatch(groups::contains);
    }

}
