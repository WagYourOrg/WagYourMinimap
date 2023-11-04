package xyz.wagyourtail.minimap.client.gui.hud.map;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec3;
import xyz.wagyourtail.minimap.client.gui.hud.overlay.AbstractMinimapOverlay;
import xyz.wagyourtail.minimap.client.gui.hud.overlay.SquareMapBorderOverlay;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSquareMapRenderer extends AbstractMinimapRenderer {
    private static final float sqrt_2 = (float) Math.sqrt(2);

    protected AbstractSquareMapRenderer(boolean rotate) {
        super(rotate, rotate ? sqrt_2 : 1, rotate);
        this.availableOverlays.add(SquareMapBorderOverlay.class);
    }

    @Override
    public List<AbstractMinimapOverlay> getDefaultOverlays() {
        List<AbstractMinimapOverlay> list = new ArrayList<>(List.of(
            new SquareMapBorderOverlay(this)
        ));
        list.addAll(super.getDefaultOverlays());
        return list;
    }

    @Override
    public void drawStencil(GuiGraphics stack, float maxLength) {
        rect(stack, 0, 0, maxLength, maxLength);
    }

    @Override
    public float getScaleForVecToBorder(Vec3 in, int chunkRadius, float maxLength) {
        return ((chunkRadius - 1) * 16f) / (float) Math.max(Math.abs(in.x), Math.abs(in.z));
    }

}
