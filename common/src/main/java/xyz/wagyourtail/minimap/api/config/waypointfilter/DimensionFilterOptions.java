package xyz.wagyourtail.minimap.api.config.waypointfilter;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.waypoint.filters.DimensionFilter;

@SettingsContainer("gui.wagyourminimap.settings.waypoint_filter.dimension")
public class DimensionFilterOptions extends AbstractWaypointFilterOptions<DimensionFilter> {

    @Override
    public DimensionFilter compileFilter() {
        return new DimensionFilter();
    }

}
