package xyz.wagyourtail.minimap.client.gui.screen.map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import xyz.wagyourtail.minimap.WagYourMinimap;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.client.gui.AbstractMapRenderer;
import xyz.wagyourtail.minimap.waypoint.Waypoint;

import java.util.Set;

public class WaypointOverlay extends AbstractFullscreenOverlay {
    private static final ResourceLocation waypoint_tex = new ResourceLocation(
        WagYourMinimap.MOD_ID,
        "textures/waypoint.png"
    );

    public WaypointOverlay(ScreenMapRenderer parent) {
        super(parent);
    }

    @Override
    public void renderOverlay(PoseStack stack, int mouseX, int mouseY) {
        float endX = parent.topX + parent.xDiam;
        float endZ = parent.topZ + parent.zDiam;

        double coordScale = minecraft.level.dimensionType().coordinateScale();
        Set<Waypoint> visible = MinimapApi.getInstance().getMapServer().waypoints.getVisibleWaypoints();
        for (Waypoint point : MinimapApi.getInstance().getMapServer().waypoints.getAllWaypoints()) {
            BlockPos pos = point.posForCoordScale(coordScale);
            if (pos.getX() > parent.topX && pos.getX() < endX && pos.getZ() > parent.topZ && pos.getZ() < endZ) {
                stack.pushPose();

                stack.translate(
                    (pos.getX() - parent.topX) * parent.chunkWidth / 16f,
                    (pos.getZ() - parent.topZ) * parent.chunkWidth / 16f,
                    0
                );
                stack.scale(.75f, .75f, 1);
                RenderSystem.setShaderTexture(0, waypoint_tex);
                int abgr = point.colB & 0xFF << 0x10 | point.colG & 0xFF << 0x8 | point.colR & 0xFF;
                if (visible.contains(point)) {
                    abgr = 0xFF000000 | abgr;
                } else {
                    abgr = 0x7F000000 | abgr;
                }
                AbstractMapRenderer.drawTexCol(stack, -10, -10, 20, 20, 0, 0, 1, 1, abgr);
                stack.scale(.75f, .75f, 1f);
                minecraft.font.draw(stack, point.name, -minecraft.font.width(point.name) / 2f, 15, 0xFFFFFF);
                stack.popPose();
            }
        }
    }


}
