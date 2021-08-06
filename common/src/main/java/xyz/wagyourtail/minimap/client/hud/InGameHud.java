package xyz.wagyourtail.minimap.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import org.jetbrains.annotations.NotNull;
import xyz.wagyourtail.minimap.client.WagYourMinimapClient;

public class InGameHud extends AbstractMapRenderer {

    public InGameHud(WagYourMinimapClient parent) {
        super(parent);
    }

    @Override
    public void render(@NotNull PoseStack matrixStack, float tickDelta) {
        matrixStack.pushPose();
        int w = client.getWindow().getGuiScaledWidth();
        int h = client.getWindow().getGuiScaledHeight();

        int minimapSize = (int) (Math.min(w, h) * parent.config.mapScreenPercent);

        LocalPlayer player = client.player;
        assert player != null;
        renderMinimap(matrixStack, player, w, h, minimapSize);
        renderPlayerPosUnderMap(matrixStack, player, w, h, minimapSize);
        matrixStack.popPose();

    }

    public void renderMinimap(PoseStack matrixStack, @NotNull LocalPlayer player, int w, int h, int minimapSize) {
        int chunkX = player.getBlockX() >> 4;
        int chunkZ = player.getBlockZ() >> 4;

        int posX = parent.config.snapSide.right ? w - minimapSize - 5 : parent.config.snapSide.center ? w / 2 - minimapSize / 2 : 5;
        int posZ = parent.config.snapSide.bottom ? h - minimapSize - client.font.lineHeight - 10 : 5;

        //TODO add circular mode and toggle
        //TODO actually draw a texture to this...
        GuiComponent.fill(matrixStack, posX, posZ, posX + minimapSize, posZ + minimapSize, 0xFFFFFFFF);
    }

    public void renderPlayerPosUnderMap(PoseStack matrixStack, @NotNull LocalPlayer player, int w, int h, int minimapSize) {
        String pos = String.format("%.2f %.2f %.2f", player.getX(), player.getY(), player.getZ());
        int width = client.font.width(pos);
        int xPos = parent.config.snapSide.right ? w - width - 5 : parent.config.snapSide.center ? w / 2 - width / 2 : 5;
        int yPos = parent.config.snapSide.bottom ? h - client.font.lineHeight - 5 : minimapSize + 10;
        client.font.draw(matrixStack, pos, xPos, yPos, 0xFFFFFF);
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
