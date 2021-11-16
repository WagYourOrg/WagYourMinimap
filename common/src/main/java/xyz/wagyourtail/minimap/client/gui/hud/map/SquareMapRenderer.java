package xyz.wagyourtail.minimap.client.gui.hud.map;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;
import xyz.wagyourtail.minimap.client.gui.hud.overlay.AbstractMinimapOverlay;
import xyz.wagyourtail.minimap.client.gui.hud.overlay.SquareMapBorderOverlay;

public class SquareMapRenderer extends AbstractMinimapRenderer {
    private static final float sqrt_2 = (float) Math.sqrt(2);

    public SquareMapRenderer(boolean rotate) {
        super(rotate, rotate ? sqrt_2 : 1, rotate);
        overlays = new AbstractMinimapOverlay[] {new SquareMapBorderOverlay(this)};
    }


    @Override
    public void drawStencil(PoseStack stack, float maxLength) {
        rect(stack, 0, 0, maxLength, maxLength);
    }

    @Override
    public float getScaleForVecToBorder(Vec3 in, int chunkRadius, float maxLength) {
        return ((chunkRadius - 1) * 16f) / (float) Math.max(Math.abs(in.x), Math.abs(in.z));
    }

}
