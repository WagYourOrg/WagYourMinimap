package xyz.wagyourtail.minimap.client.gui.hud.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.WagYourMinimap;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.api.client.config.MinimapClientConfig;
import xyz.wagyourtail.minimap.client.gui.AbstractMapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.map.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.waypoint.Waypoint;

import java.util.Set;

@SettingsContainer("gui.wagyourminimap.settings.overlay.waypoint")
public class WaypointOverlay extends AbstractMinimapOverlay {
    private static final ResourceLocation waypoint_arrow_tex = new ResourceLocation(
        WagYourMinimap.MOD_ID,
        "textures/waypoint_arrow.png"
    );


    public WaypointOverlay(AbstractMinimapRenderer parent) {
        super(parent);
    }

    @Override
    public void renderOverlay(GuiGraphics stack, @NotNull Vec3 center, float maxLength, @NotNull Vec3 player_pos, float player_rot) {
        int chunkRadius = MinimapClientApi.getInstance().getConfig().get(MinimapClientConfig.class).chunkRadius;
        int chunkDiam = chunkRadius * 2 - 1;
        float chunkScale = maxLength / ((float) chunkDiam - 1);

        Set<Waypoint> points = MinimapApi.getInstance().getMapServer().waypoints.getVisibleWaypoints();
        for (Waypoint point : points) {
            stack.pose().pushPose();
            BlockPos pos = point.posForCoordScale(minecraft.level.dimensionType().coordinateScale());
            Vec3 pointVec = new Vec3(pos.getX(), pos.getY(), pos.getZ()).subtract(center);
            if (parent.rotate) {
                pointVec = pointVec.yRot((float) Math.toRadians(
                    player_rot - 180));
            }
            if (parent.scaleBy != 1) {
                pointVec = pointVec.multiply(parent.scaleBy, 1, parent.scaleBy);
            }
            float scale = parent.getScaleForVecToBorder(pointVec, chunkRadius, maxLength);
            if (scale < 1) {
                pointVec = pointVec.multiply(scale, 1, scale);
            }
            stack.pose().translate(
                maxLength / 2 + pointVec.x * chunkScale / 16f,
                maxLength / 2 + pointVec.z * chunkScale / 16f,
                0
            );
            stack.pose().scale(.004f * maxLength, .004f * maxLength, 1);
            if (scale < 1) {
//                stack.mulPose(Vector3f.ZN.rotation((float) Math.atan2(pointVec.x, pointVec.z)));
                stack.pose().mulPose(new Quaternionf().rotateZ(-(float) Math.atan2(pointVec.x, pointVec.z)));
                RenderSystem.setShaderTexture(0, waypoint_arrow_tex);
            } else {
                RenderSystem.setShaderTexture(0, point.getIcon());
            }
            int abgr = 0xFF000000 | point.colB & 0xFF << 0x10 | point.colG & 0xFF << 0x8 | point.colR & 0xFF;
            AbstractMapRenderer.drawTexCol(stack, -10, -10, 20, 20, 0, 0, 1, 1, abgr);
            if (scale >= 1) {
                stack.drawString(minecraft.font, point.name, -minecraft.font.width(point.name) / 2, 10, 0xFFFFFF);
            }
            stack.pose().popPose();
        }
    }

}
