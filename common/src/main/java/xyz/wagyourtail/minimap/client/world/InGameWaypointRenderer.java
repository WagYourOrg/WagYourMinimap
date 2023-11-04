package xyz.wagyourtail.minimap.client.world;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.api.client.config.MinimapClientConfig;
import xyz.wagyourtail.minimap.client.gui.AbstractMapRenderer;
import xyz.wagyourtail.minimap.waypoint.Waypoint;

public class InGameWaypointRenderer {
    private static final Minecraft mc = Minecraft.getInstance();
    public static final Event<RenderLastEvent> RENDER_LAST = EventFactory.createLoop();
    public static final double WARP_COMPENSATION_Y_FACTOR = 70;
    public static final double WARP_COMPENSATION_X_FACTOR = 10;


    public static void onRender(GuiGraphics stack, Camera camera) {
        stack.pose().pushPose();
        Vec3 center = camera.getPosition();
        boolean showBeam = MinimapClientApi.getInstance().getConfig().get(MinimapClientConfig.class).showWaypointBeam;
        for (Waypoint visibleWaypoint : MinimapClientApi.getInstance().getMapServer().waypoints.getVisibleWaypoints()) {
            //move center to waypoint
            assert mc.level != null;
            BlockPos pos = visibleWaypoint.posForCoordScale(mc.level.dimensionType().coordinateScale());
            Vec3 offset = new Vec3(pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f).subtract(center);
            double xz = Math.sqrt(offset.x * offset.x + offset.z * offset.z);
            float xRot = 90F - (float) Mth.wrapDegrees(Math.toDegrees(Math.atan2(xz, -offset.y)));
            float yRot = (float) Mth.wrapDegrees(Math.toDegrees(Math.atan2(offset.x, offset.z)));
            // because of view warping we want to scale down as the y rot increases, using equation:
            //                      factor = yFactor / 180 * abs(relative_camera_yRot) + xFactor / 180 * abs(relative_camera_xRot) + 20;
            // this is a bit of a hack, but it works.
            // this warping is more apparent as the FOV increases
            // if you want to see how worse it really is, try making it statically set to 20 and quake pro
            double factor =
                WARP_COMPENSATION_Y_FACTOR / 180d * Math.abs(Mth.wrapDegrees(
                    mc.gameRenderer.getMainCamera().getYRot() + yRot)) +
                    WARP_COMPENSATION_X_FACTOR / 180d * Math.abs(mc.gameRenderer.getMainCamera().getXRot() - xRot) +
                    20;
            Vec3 normalized_offset = offset.normalize().multiply(factor, factor, factor);
            double distance = offset.distanceTo(Vec3.ZERO);


            // render waypoint beam at it's actual location in the world
            if (showBeam && distance < 256) {
                stack.pose().pushPose();
                RenderSystem.enableDepthTest();
                stack.pose().translate(offset.x, offset.y, offset.z);
                renderWaypointBeam(stack, center, xRot, yRot, visibleWaypoint, distance);
                stack.pose().popPose();
            }

            if (distance > factor) {
                // render waypoint relative to the player so infinite distance works
                stack.pose().pushPose();
                RenderSystem.disableDepthTest();
                stack.pose().translate(normalized_offset.x, normalized_offset.y, normalized_offset.z);
                renderWaypointIcon(stack, offset, xRot, yRot, visibleWaypoint, distance);
                stack.pose().popPose();
            } else {
                stack.pose().pushPose();
                RenderSystem.disableDepthTest();
                stack.pose().translate(offset.x, offset.y, offset.z);
                stack.pose().scale((float) (distance / factor), (float) (distance / factor), (float) (distance / factor));
                renderWaypointIcon(stack, offset, xRot, yRot, visibleWaypoint, distance);
                stack.pose().popPose();
            }
        }
        RenderSystem.enableDepthTest();
        stack.pose().popPose();
    }

    public static void renderWaypointIcon(GuiGraphics stack, Vec3 offset, float xRot, float yRot, Waypoint waypoint, double distance) {
        stack.pose().mulPose(new Quaternionf().rotateY((float) Math.toRadians(yRot)));
        stack.pose().mulPose(new Quaternionf().rotateX((float) Math.toRadians(xRot)));
        stack.pose().mulPose(new Quaternionf().rotateZ((float) Math.toRadians(180)));
        float scale = (float) Math.max(.0675, -distance / 50f * .0625 + .125f);
        stack.pose().scale(scale, scale, scale);
        RenderSystem.setShaderTexture(0, waypoint.getIcon());
        int abgr = 0xFF000000 | waypoint.colB & 0xFF << 0x10 | waypoint.colG & 0xFF << 0x8 | waypoint.colR & 0xFF;
        AbstractMapRenderer.drawTexCol(stack, -10, -10, 20, 20, 0, 0, 1, 1, abgr);
        if (isLookingAt(
            offset.normalize(),
            mc.gameRenderer.getMainCamera().getXRot(),
            mc.gameRenderer.getMainCamera().getYRot()
        )) {
            drawText(stack, String.format("%s (%.2f m)", waypoint.name, offset.distanceTo(Vec3.ZERO)));
        }
    }

    private static boolean isLookingAt(Vec3 normal, float xRot, float yRot) {
        return normal.distanceTo(Vec3.directionFromRotation(xRot, yRot)) < .1;
    }

    public static void drawText(GuiGraphics stack, String text) {
        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        mc.font.drawInBatch(
            text,
            -mc.font.width(text) / 2f,
            20,
            -1,
            false,
            stack.pose().last().pose(),
            buffer,
            Font.DisplayMode.NORMAL,
            0x30000000,
            0xF000F0
        );
        buffer.endBatch();
    }

    public static void renderWaypointBeam(GuiGraphics stack, Vec3 offset, float xRot, float yRot, Waypoint waypoint, double distance) {
        // face player

//        stack.mulPose(Vector3f.YP.rotationDegrees(yRot));
//        stack.mulPose(Vector3f.ZP.rotationDegrees(180));
        stack.pose().mulPose(new Quaternionf().rotateY((float) Math.toRadians(yRot)));
        stack.pose().mulPose(new Quaternionf().rotateZ((float) Math.toRadians(180)));

        //        float scale = (float) Math.max(.0675, -distance / 50f * .0625 + .125f);
        //        stack.scale(scale, scale, scale);

        // rendering code
        Matrix4f matrix = stack.pose().last().pose();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float r = (waypoint.colR & 0xFF) / 255f;
        float g = (waypoint.colG & 0xFF) / 255f;
        float b = (waypoint.colB & 0xFF) / 255f;
        builder.vertex(matrix, 0, 2048, 0).color(r, g, b, 0.75f).endVertex();
        builder.vertex(matrix, 0, -2048, 0).color(r, g, b, 0.75f).endVertex();
        builder.vertex(matrix, -.5f, -2048, 0).color(r, g, b, 0.0f).endVertex();
        builder.vertex(matrix, -.5f, 2048, 0).color(r, g, b, 0.0f).endVertex();
        builder.vertex(matrix, 0, 2048, 0).color(r, g, b, 0.75f).endVertex();
        builder.vertex(matrix, .5f, 2048, 0).color(r, g, b, 0.0f).endVertex();
        builder.vertex(matrix, .5f, -2048, 0).color(r, g, b, 0.0f).endVertex();
        builder.vertex(matrix, 0, -2048, 0).color(r, g, b, 0.75f).endVertex();
        BufferUploader.drawWithShader(builder.end());
    }

    public interface RenderLastEvent {
        void onRenderLast(PoseStack stack, Camera camera);

    }

}
