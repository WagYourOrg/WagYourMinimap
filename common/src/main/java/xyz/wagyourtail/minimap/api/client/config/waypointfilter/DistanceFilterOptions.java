package xyz.wagyourtail.minimap.api.client.config.waypointfilter;

import xyz.wagyourtail.config.field.IntRange;
import xyz.wagyourtail.config.field.Setting;
import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.waypoint.filters.DistanceFilter;

@SettingsContainer("gui.wagyourminimap.settings.waypoint_filter.distance")
public class DistanceFilterOptions extends AbstractWaypointFilterOptions<DistanceFilter> {

    @Setting(value = "gui.wagyourminimap.settings.distancefilter.max")
    @IntRange(from = 1000, to = 100_000, stepVal = 1000)
    public int max = 1000;

    @Override
    public DistanceFilter compileFilter() {
        return new DistanceFilter(max);
    }

}
