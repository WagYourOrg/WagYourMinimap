package xyz.wagyourtail.minimap.client.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.waypoint.Waypoint;

public class WaypointEditScreen extends Screen {
    private final Screen parent;
    private final Waypoint prev_point;



    public WaypointEditScreen(Screen parent, Waypoint prev_point) {
        super(new TranslatableComponent("gui.wagyourminimap.waypoint_edit"));
        this.parent = parent;
        this.prev_point = prev_point;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    public Waypoint compileWaypoint() {
        throw new RuntimeException("NOT YET IMPLEMENTED");
        //TODO: return new Waypoint();
    }

    @Override
    public void onClose() {
        MinimapApi.getInstance().getMapServer().waypoints.removeWaypoint(prev_point);
        MinimapApi.getInstance().getMapServer().waypoints.addWaypoint(compileWaypoint());
        minecraft.setScreen(parent);
    }

    @Override
    protected void init() {
        super.init();
    }

}
