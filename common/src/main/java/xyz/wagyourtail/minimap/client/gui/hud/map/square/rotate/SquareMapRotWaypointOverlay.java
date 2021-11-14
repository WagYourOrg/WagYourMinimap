package xyz.wagyourtail.minimap.client.gui.hud.map.square.rotate;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import xyz.wagyourtail.minimap.WagYourMinimap;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.api.client.config.MinimapClientConfig;
import xyz.wagyourtail.minimap.client.gui.AbstractMapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.map.AbstractMapOverlayRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.map.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.waypoint.Waypoint;

import java.util.Set;

public class SquareMapRotWaypointOverlay extends AbstractMapOverlayRenderer {
    private static final float sqrt_2 = (float) Math.sqrt(2);

    private static final ResourceLocation waypoint_tex = new ResourceLocation(WagYourMinimap.MOD_ID,
        "textures/waypoint.png"
    );
    private static final ResourceLocation waypoint_arrow_tex = new ResourceLocation(WagYourMinimap.MOD_ID,
        "textures/waypoint_arrow.png"
    );


    public SquareMapRotWaypointOverlay(AbstractMinimapRenderer parent) {
        super(parent);
    }

    @Override
    public void renderOverlay(PoseStack stack, @NotNull Vec3 center, float maxLength, @NotNull Vec3 player_pos, float player_rot) {
        int chunkRadius = MinimapClientApi.getInstance().getConfig().get(MinimapClientConfig.class).chunkRadius;

        int chunkDiam = chunkRadius * 2 - 1;
        float chunkScale = maxLength / ((float) chunkDiam - 1);

        Set<Waypoint> points = MinimapApi.getInstance().getMapServer().waypoints.getVisibleWaypoints();
        for (Waypoint point : points) {
            stack.pushPose();
            BlockPos pos = point.posForCoordScale(minecraft.level.dimensionType().coordinateScale());
            Vec3 pointVec = new Vec3(pos.getX(), pos.getY(), pos.getZ()).subtract(center).yRot((float) Math.toRadians(
                player_rot - 180));
            pointVec = pointVec.multiply(sqrt_2, sqrt_2, sqrt_2);
            float scale = ((chunkRadius - 1) * 16f) / (float) Math.max(Math.abs(pointVec.x), Math.abs(pointVec.z));
            if (scale < 1) {
                pointVec = pointVec.multiply(scale, scale, scale);
            }
            stack.translate(maxLength / 2 + pointVec.x * chunkScale / 16f,
                maxLength / 2 + pointVec.z * chunkScale / 16f,
                0
            );
            stack.scale(.0025f * maxLength, .0025f * maxLength, 1);
            if (scale < 1) {
                stack.mulPose(Vector3f.ZN.rotation((float) Math.atan2(pointVec.x, pointVec.z)));
                RenderSystem.setShaderTexture(0, waypoint_arrow_tex);
            } else {
                RenderSystem.setShaderTexture(0, waypoint_tex);
            }
            int abgr = 0xFF000000 | point.colB & 0xFF << 0x10 | point.colG & 0xFF << 0x8 | point.colR & 0xFF;
            AbstractMapRenderer.drawTexCol(stack, -10, -10, 20, 20, 1, 1, 0, 0, abgr);
            if (scale >= 1) {
                minecraft.font.draw(stack, point.name, -minecraft.font.width(point.name) / 2f, 10, 0xFFFFFF);
            }
            stack.popPose();
        }
    }

}
