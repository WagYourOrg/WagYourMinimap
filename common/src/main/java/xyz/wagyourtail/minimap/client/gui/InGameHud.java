package xyz.wagyourtail.minimap.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import xyz.wagyourtail.minimap.client.gui.renderer.AbstractMinimapRenderer;

public class InGameHud {
    protected final Minecraft client = Minecraft.getInstance();
    protected AbstractMinimapRenderer renderer;

    public AbstractMinimapRenderer getRenderer() {
        return renderer;
    }

    public void setRenderer(AbstractMinimapRenderer renderer) {
        this.renderer = renderer;
    }

    public void render(@NotNull PoseStack matrixStack, float tickDelta) {
        renderer.render(matrixStack, tickDelta);
    }

    public enum SnapSide {
        TOP_LEFT(false, false, false), TOP_CENTER(false, true, false), TOP_RIGHT(true, false, false),
        BOTTOM_LEFT(false, false, true), BOTTOM_RIGHT(true, false, true);

        public final boolean right, center, bottom;

        SnapSide(boolean right, boolean center, boolean bottom) {
            this.right = right;
            this.center = center;
            this.bottom = bottom;
        }
    }

}
