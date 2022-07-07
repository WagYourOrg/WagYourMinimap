package xyz.wagyourtail.minimap.client.gui.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;
import xyz.wagyourtail.minimap.client.gui.hud.map.AbstractMinimapRenderer;

public class InGameHud {
    protected static AbstractMinimapRenderer renderer;
    public static boolean shouldRender;

    public static AbstractMinimapRenderer getRenderer() {
        return renderer;
    }

    public static void setRenderer(AbstractMinimapRenderer renderer) {
        InGameHud.renderer = renderer;
    }

    public static void render(@NotNull PoseStack matrixStack, float tickDelta) {
        renderer.render(matrixStack, tickDelta);
    }

    public enum SnapSide {
        TOP_LEFT(false, false, false),
        TOP_CENTER(false, true, false),
        TOP_RIGHT(true, false, false),
        BOTTOM_LEFT(false, false, true),
        BOTTOM_RIGHT(true, false, true);

        public final boolean right, center, bottom;

        SnapSide(boolean right, boolean center, boolean bottom) {
            this.right = right;
            this.center = center;
            this.bottom = bottom;
        }
    }

}
