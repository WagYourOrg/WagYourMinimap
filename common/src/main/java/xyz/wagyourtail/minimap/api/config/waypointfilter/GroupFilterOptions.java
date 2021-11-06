package xyz.wagyourtail.minimap.api.config.waypointfilter;

import xyz.wagyourtail.config.field.Setting;
import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.waypoint.filters.GroupFilter;

import java.util.Set;

@SettingsContainer("gui.wagyourminimap.settings.waypoint_filter.group")
public class GroupFilterOptions extends AbstractWaypointFilterOptions<GroupFilter> {

    @Setting(value = "gui.wagyourminimap.settings.groupfilter.groups")
    public String[] groups = new String[]{ "default" };

    @Override
    public GroupFilter compileFilter() {
        return new GroupFilter(Set.of(groups));
    }

}
