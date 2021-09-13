package xyz.wagyourtail.minimap.waypoint.filters;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import xyz.wagyourtail.minimap.waypoint.Waypoint;

import java.util.function.Predicate;

public class DistanceFilter implements Predicate<Waypoint> {
    protected static Minecraft minecraft = Minecraft.getInstance();
    private final int distance;
    public DistanceFilter(int distance) {
        this.distance = distance;
    }

    @Override
    public boolean test(Waypoint waypoint) {
        return new Vec3(waypoint.posX(), waypoint.posY(), waypoint.posZ()).distanceTo(minecraft.player.position()) < distance;
    }

}
