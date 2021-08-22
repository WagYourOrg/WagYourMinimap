package xyz.wagyourtail.minimap.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Option;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import xyz.wagyourtail.minimap.WagYourMinimap;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.client.gui.renderer.AbstractMapRenderer;
import xyz.wagyourtail.minimap.waypoint.Waypoint;

public class InGameWaypointRenderer {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final ResourceLocation waypoint_tex = new ResourceLocation(WagYourMinimap.MOD_ID, "textures/waypoint.png");
    public static final Event<RenderLastEvent> RENDER_LAST = EventFactory.createLoop();


    public void onRender(PoseStack stack, float partialTicks, long finishTimeNano) {
        assert mc.cameraEntity != null;
        Vec3 center = mc.gameRenderer.getMainCamera().getPosition();
        float xRot = mc.gameRenderer.getMainCamera().getXRot();
        float yRot = mc.gameRenderer.getMainCamera().getYRot();
        RenderSystem.setShaderTexture(0, waypoint_tex);
        RenderSystem.disableDepthTest();
        for (Waypoint visibleWaypoint : MinimapClientApi.getInstance().getMapServer().waypoints.getVisibleWaypoints()) {
            stack.pushPose();
                renderWaypoint(stack, center, xRot, yRot, visibleWaypoint);
            stack.popPose();
        }
        RenderSystem.enableDepthTest();
    }

    public void renderWaypoint(PoseStack stack, Vec3 center, float xRot, float yRot, Waypoint waypoint) {
        Vec3 offset = new Vec3(waypoint.posX(), waypoint.posY(), waypoint.posZ()).subtract(center);
        stack.translate(offset.x, offset.y, offset.z);
        stack.mulPose(Vector3f.YP.rotationDegrees(-yRot));
        stack.mulPose(Vector3f.XP.rotationDegrees(xRot));
        double maxDistance = mc.options.renderDistance * 16.0D * 0.99D;


        AbstractMapRenderer.drawTexCol(stack, -.1f, -.1f, .2f, .2f, 0, 0, 1, 1, 0xFFFFFFFF);
    }

    public interface RenderLastEvent {
        void onRenderLast(PoseStack stack, float partialTicks, long finishTimeNano);
    }
}
