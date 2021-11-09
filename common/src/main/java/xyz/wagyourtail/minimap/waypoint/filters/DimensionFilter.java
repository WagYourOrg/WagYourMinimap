package xyz.wagyourtail.minimap.waypoint.filters;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.map.MapServer;
import xyz.wagyourtail.minimap.waypoint.Waypoint;

import java.util.Arrays;
import java.util.function.Predicate;

public class DimensionFilter extends WaypointFilter {
    @Override
    public boolean test(Waypoint waypoint) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return false;
        String levelname = MinimapApi.getInstance().getMapServer().currentLevelNameSupplier.get();
        return Arrays.asList(waypoint.levels).contains(levelname);
    }

}
