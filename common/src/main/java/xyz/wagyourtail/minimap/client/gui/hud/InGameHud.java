package xyz.wagyourtail.minimap.client.gui.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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

    public static void render(@NotNull GuiGraphics matrixStack, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        try {
            renderer.render(matrixStack, tickDelta);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
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
