package xyz.wagyourtail.minimap.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import org.jetbrains.annotations.NotNull;
import xyz.wagyourtail.minimap.client.WagYourMinimapClient;
import xyz.wagyourtail.minimap.scanner.ChunkData;
import xyz.wagyourtail.minimap.scanner.MapLevel;
import xyz.wagyourtail.minimap.scanner.MapRegion;

import java.util.concurrent.ExecutionException;

public class InGameHud extends AbstractMapGui {

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
        try {
            MapRegion region = parent.currentLevel.getRegion(new MapLevel.Pos(chunkX >> 5, chunkZ >> 5));
            ChunkData chunk = region.data[MapRegion.chunkPosToIndex(chunkX, chunkZ)];
            if (chunk != null)
                bindChunkTex(chunk);
        } catch (ExecutionException ignored) {}

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        GuiComponent.blit(matrixStack, posX, posZ,posX + minimapSize, posZ + minimapSize, 0, 0, 16, 16, 16,16);
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
