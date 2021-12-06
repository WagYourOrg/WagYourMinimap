package xyz.wagyourtail.minimap.waypoint.filters;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.waypoint.Waypoint;

import java.util.Arrays;

@SettingsContainer("gui.wagyourminimap.settings.waypoint_filter.dimension")
public class DimensionFilter extends WaypointFilter {
    @Override
    public boolean test(Waypoint waypoint) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return false;
        }
        String levelname = MinimapApi.getInstance().getMapServer().levelNameSupplier.get();
        return Arrays.asList(waypoint.levels).contains(levelname);
    }

}
