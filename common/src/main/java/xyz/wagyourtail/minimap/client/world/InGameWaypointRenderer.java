package xyz.wagyourtail.minimap.client.world;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import xyz.wagyourtail.minimap.WagYourMinimap;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.api.client.config.MinimapClientConfig;
import xyz.wagyourtail.minimap.client.gui.AbstractMapRenderer;
import xyz.wagyourtail.minimap.waypoint.Waypoint;

public class InGameWaypointRenderer {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final ResourceLocation waypoint_tex = new ResourceLocation(WagYourMinimap.MOD_ID, "textures/waypoint.png");
    public static final Event<RenderLastEvent> RENDER_LAST = EventFactory.createLoop();


    public static void onRender(PoseStack stack, float partialTicks, long finishTimeNano) {
        stack.pushPose();
        assert mc.cameraEntity != null;
        Vec3 center = mc.gameRenderer.getMainCamera().getPosition();
        float xRot = mc.gameRenderer.getMainCamera().getXRot();
        float yRot = mc.gameRenderer.getMainCamera().getYRot();
        RenderSystem.disableDepthTest();
        boolean showBeam = MinimapClientApi.getInstance().getConfig().get(MinimapClientConfig.class).showWaypointBeam;
        for (Waypoint visibleWaypoint : MinimapClientApi.getInstance().getMapServer().waypoints.getVisibleWaypoints()) {
            stack.pushPose();
            //move center to waypoint
            BlockPos pos = visibleWaypoint.posForCoordScale(mc.level.dimensionType().coordinateScale());
            Vec3 offset = new Vec3(pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f).subtract(center);
            Vec3 normalized_offset = offset.normalize().multiply(20, 20, 20);
            stack.translate(normalized_offset.x, normalized_offset.y, normalized_offset.z);
            double distance = offset.distanceTo(Vec3.ZERO);

            if (showBeam && distance < 256) {
                stack.pushPose();
                renderWaypointBeam(stack, center, xRot, yRot, visibleWaypoint, distance);
                stack.popPose();
            }

            stack.pushPose();
            renderWaypointIcon(stack, offset, xRot, yRot, visibleWaypoint, distance);
            stack.popPose();
            stack.popPose();
        }
        RenderSystem.enableDepthTest();
        stack.popPose();
    }

    public static void renderWaypointIcon(PoseStack stack, Vec3 offset, float xRot, float yRot, Waypoint waypoint, double distance) {
        stack.mulPose(Vector3f.YP.rotationDegrees(-yRot));
        stack.mulPose(Vector3f.XP.rotationDegrees(xRot));
        stack.mulPose(Vector3f.ZP.rotationDegrees(180));
        float scale = (float) Math.max(.0675, -distance / 50f * .0625 + .125f);
        stack.scale(scale, scale, scale);
        RenderSystem.setShaderTexture(0, waypoint_tex);
        int abgr = 0xFF000000 | waypoint.colB & 0xFF << 0x10 | waypoint.colG & 0xFF << 0x8 | waypoint.colR & 0xFF;
        AbstractMapRenderer.drawTexCol(stack, -10, -10, 20, 20, 0, 0, 1, 1, abgr);
        if (isLookingAt(offset.normalize(), xRot, yRot)) {
            drawText(stack, String.format("%s (%.2f m)", waypoint.name, offset.distanceTo(Vec3.ZERO)));
        }
    }

    public static void renderWaypointBeam(PoseStack stack, Vec3 offset, float xRot, float yRot, Waypoint waypoint, double distance) {
        stack.mulPose(Vector3f.YP.rotationDegrees(-yRot));
//        stack.mulPose(Vector3f.XP.rotationDegrees(xRot));
        stack.mulPose(Vector3f.ZP.rotationDegrees(180));
        float scale = (float) Math.max(.0675, -distance / 50f * .0625 + .125f);
        stack.scale(scale, scale, scale);
        Matrix4f matrix = stack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float r = (waypoint.colR & 0xFF) / 255f;
        float g =  (waypoint.colG & 0xFF) / 255f;
        float b = (waypoint.colB & 0xFF) / 255f;
        builder.vertex(matrix, 0, 2048, 0).color(r, g, b, 0.75f).endVertex();
        builder.vertex(matrix, 0, -2048, 0).color(r, g, b, 0.75f).endVertex();
        builder.vertex(matrix, -14, -2048, 0).color(r, g, b, 0.0f).endVertex();
        builder.vertex(matrix, -14, 2048, 0).color(r, g, b, 0.0f).endVertex();
        builder.vertex(matrix, 0, 2048, 0).color(r, g, b, 0.75f).endVertex();
        builder.vertex(matrix, 14, 2048, 0).color(r, g, b, 0.0f).endVertex();
        builder.vertex(matrix, 14, -2048, 0).color(r, g, b, 0.0f).endVertex();
        builder.vertex(matrix, 0, -2048, 0).color(r, g, b, 0.75f).endVertex();
        builder.end();
        BufferUploader.end(builder);
    }

    private static boolean isLookingAt(Vec3 normal, float xRot, float yRot) {
        return normal.distanceTo(Vec3.directionFromRotation(xRot, yRot)) < .1;
    }

    public static void drawText(PoseStack stack, String text) {
        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        mc.font.drawInBatch(text, -mc.font.width(text) / 2f, 20, -1, false, stack.last().pose(), buffer, true, 0x20000000, 0xF000F0);
        buffer.endBatch();
    }

    public interface RenderLastEvent {
        void onRenderLast(PoseStack stack, float partialTicks, long finishTimeNano);

    }

}
