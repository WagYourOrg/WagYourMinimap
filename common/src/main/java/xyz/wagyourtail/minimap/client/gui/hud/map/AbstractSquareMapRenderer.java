package xyz.wagyourtail.minimap.client.gui.hud.map;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;
import xyz.wagyourtail.minimap.client.gui.hud.overlay.AbstractMinimapOverlay;
import xyz.wagyourtail.minimap.client.gui.hud.overlay.SquareMapBorderOverlay;
import xyz.wagyourtail.minimap.map.image.ImageStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class AbstractSquareMapRenderer extends AbstractMinimapRenderer {
    private static final float sqrt_2 = (float) Math.sqrt(2);

    protected AbstractSquareMapRenderer(boolean rotate, Set<Class<? extends ImageStrategy>> layers, Set<Class<? extends AbstractMinimapOverlay>> overlays) {
        super(rotate, rotate ? sqrt_2 : 1, rotate, layers, Sets.union(Set.of(SquareMapBorderOverlay.class), overlays));
    }


    @Override
    public void drawStencil(PoseStack stack, float maxLength) {
        rect(stack, 0, 0, maxLength, maxLength);
    }

    @Override
    public float getScaleForVecToBorder(Vec3 in, int chunkRadius, float maxLength) {
        return ((chunkRadius - 1) * 16f) / (float) Math.max(Math.abs(in.x), Math.abs(in.z));
    }

    @Override
    public List<AbstractMinimapOverlay> getDefaultOverlays() {
        List<AbstractMinimapOverlay> list = new ArrayList<>(List.of(
            new SquareMapBorderOverlay(this)
        ));
        list.addAll(super.getDefaultOverlays());
        return list;
    }

}
