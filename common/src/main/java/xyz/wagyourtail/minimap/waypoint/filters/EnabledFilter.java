package xyz.wagyourtail.minimap.waypoint.filters;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.waypoint.Waypoint;

@SettingsContainer("gui.wagyourminimap.settings.waypoint_filter.enabled")
public class EnabledFilter extends WaypointFilter {

    @Override
    public boolean test(Waypoint waypoint) {
        return waypoint.enabled;
    }

}
