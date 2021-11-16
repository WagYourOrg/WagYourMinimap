package xyz.wagyourtail.minimap.waypoint.filters;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import xyz.wagyourtail.minimap.waypoint.Waypoint;

public class DistanceFilter extends WaypointFilter {
    protected static Minecraft minecraft = Minecraft.getInstance();
    private final int distance;

    public DistanceFilter(int distance) {
        this.distance = distance;
    }

    @Override
    public boolean test(Waypoint waypoint) {
        BlockPos pos = waypoint.posForCoordScale(minecraft.level.dimensionType().coordinateScale());
        return new Vec3(pos.getX(), pos.getY(), pos.getZ()).distanceTo(minecraft.cameraEntity.position()) < distance;
    }

}
