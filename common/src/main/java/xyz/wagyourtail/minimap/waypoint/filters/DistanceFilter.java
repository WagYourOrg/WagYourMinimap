package xyz.wagyourtail.minimap.waypoint.filters;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import xyz.wagyourtail.config.field.IntRange;
import xyz.wagyourtail.config.field.Setting;
import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.waypoint.Waypoint;

@SettingsContainer("gui.wagyourminimap.settings.waypoint_filter.distance")
public class DistanceFilter extends WaypointFilter {
    protected static Minecraft minecraft = Minecraft.getInstance();

    @Setting(value = "gui.wagyourminimap.settings.distancefilter.max")
    @IntRange(from = 1000, to = 100_000, stepVal = 1000)
    public int max = 1000;

    @Override
    public boolean test(Waypoint waypoint) {
        BlockPos pos = waypoint.posForCoordScale(minecraft.level.dimensionType().coordinateScale());
        return new Vec3(pos.getX(), pos.getY(), pos.getZ()).distanceTo(minecraft.cameraEntity.position()) < max;
    }

}
