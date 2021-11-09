package xyz.wagyourtail.minimap.api.client.config.waypointfilter;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.waypoint.filters.EnabledFilter;

@SettingsContainer("gui.wagyourminimap.settings.waypoint_filter.enabled")
public class EnabledFilterOptions extends AbstractWaypointFilterOptions<EnabledFilter> {

    @Override
    public EnabledFilter compileFilter() {
        return new EnabledFilter();
    }

}
