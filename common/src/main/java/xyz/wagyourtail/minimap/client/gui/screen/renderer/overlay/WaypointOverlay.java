package xyz.wagyourtail.minimap.client.gui.screen.renderer.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;
import xyz.wagyourtail.minimap.WagYourMinimap;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.client.gui.renderer.AbstractMapRenderer;
import xyz.wagyourtail.minimap.client.gui.screen.renderer.AbstractFullscreenOverlay;
import xyz.wagyourtail.minimap.client.gui.screen.renderer.ScreenMapRenderer;
import xyz.wagyourtail.minimap.waypoint.Waypoint;

public class WaypointOverlay extends AbstractFullscreenOverlay {
    private static final ResourceLocation waypoint_tex = new ResourceLocation(WagYourMinimap.MOD_ID, "textures/waypoint.png");

    public WaypointOverlay(ScreenMapRenderer parent) {
        super(parent);
    }

    @Override
    public void renderOverlay(PoseStack stack, int mouseX, int mouseY) {
        float endX = parent.topX + parent.xDiam;
        float endZ = parent.topZ + parent.zDiam;


        for (Waypoint point : MinimapApi.getInstance().getMapServer().waypoints.getVisibleWaypoints()) {
            if (point.posX() > parent.topX && point.posX() < endX && point.posZ() > parent.topZ && point.posZ() < endZ) {
                stack.pushPose();

                stack.translate((point.posX() - parent.topX) * parent.chunkWidth / 16f, (point.posZ() - parent.topZ) * parent.chunkWidth / 16f, 0);
                stack.scale(.75f, .75f, 1);
                RenderSystem.setShaderTexture(0, waypoint_tex);
                int abgr = 0xFF000000 | point.colB() << 0x10 | point.colG() << 0x8 | point.colR() & 255;
                AbstractMapRenderer.drawTexCol(stack, -10, -10, 20, 20, 0, 0, 1, 1, abgr);
                stack.scale(.75f, .75f, 1f);
                minecraft.font.draw(stack, point.name(), -minecraft.font.width(point.name()) / 2f, 15, 0xFFFFFF);
                stack.popPose();
            }
        }
    }


}
