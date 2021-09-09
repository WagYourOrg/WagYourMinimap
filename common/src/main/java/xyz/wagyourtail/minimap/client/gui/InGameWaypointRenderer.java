package xyz.wagyourtail.minimap.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Vector3f;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
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
        stack.pushPose();
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
        stack.popPose();
    }

    public void renderWaypoint(PoseStack stack, Vec3 center, float xRot, float yRot, Waypoint waypoint) {
        Vec3 offset = new Vec3(waypoint.posX() + .5f, waypoint.posY() + .5f, waypoint.posZ() + .5f).subtract(center);
        Vec3 normalized_offset = offset.normalize().multiply(20, 20, 20);
        stack.translate(normalized_offset.x, normalized_offset.y, normalized_offset.z);
        stack.mulPose(Vector3f.YP.rotationDegrees(-yRot));
        stack.mulPose(Vector3f.XP.rotationDegrees(xRot));
        stack.mulPose(Vector3f.ZP.rotationDegrees(180));
        float scale = (float) Math.max(.0675, -offset.distanceTo(Vec3.ZERO) / 50f * .0625 + .125f);
        stack.scale(scale, scale, scale);
        int abgr = 0xFF000000 | waypoint.colB() << 0x10 | waypoint.colG() << 0x8 | waypoint.colR() & 255;
        AbstractMapRenderer.drawTexCol(stack, -20, -20, 40, 40, 0, 0, 1, 1, abgr);
        if (isLookingAt(offset.normalize(), xRot, yRot)) {
            drawText(stack, String.format("%s (%.2f m)", waypoint.name(), offset.distanceTo(Vec3.ZERO)));
        }
    }

    private boolean isLookingAt(Vec3 normal, float xRot, float yRot) {
        return normal.distanceTo(Vec3.directionFromRotation(xRot, yRot)) < .1;
    }

    public void drawText(PoseStack stack, String text) {
        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        mc.font.drawInBatch(text, -mc.font.width(text) / 2f, 20, -1, false, stack.last().pose(), buffer, true, 0x20000000, 0xF000F0);
        buffer.endBatch();
    }

    public interface RenderLastEvent {
        void onRenderLast(PoseStack stack, float partialTicks, long finishTimeNano);

    }

}
