package xyz.wagyourtail.minimap.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import xyz.wagyourtail.minimap.client.WagYourMinimapClient;
import xyz.wagyourtail.minimap.client.gui.renderer.TestRenderStrategy;
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
        if (client.options.renderDebug) return;
        matrixStack.pushPose();
        int w = client.getWindow().getGuiScaledWidth();
        int h = client.getWindow().getGuiScaledHeight();

        int minimapSize = (int) (Math.min(w, h) * parent.config.mapScreenPercent);

        LocalPlayer player = client.player;
        assert player != null;
        renderMinimap(matrixStack, player.getPosition(tickDelta), w, h, minimapSize);
        renderPlayerPosUnderMap(matrixStack, player.getPosition(tickDelta), w, h, minimapSize);
        //TODO: make toggle
        renderDebugInfo(matrixStack, player.getPosition(tickDelta), w, h, minimapSize);
        matrixStack.popPose();
    }

    public void renderMinimap(PoseStack matrixStack, @NotNull Vec3 player, int w, int h, int minimapSize) {
        int chunkX = ((int) player.x) >> 4;
        int chunkZ = ((int) player.z) >> 4;

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

    public void renderPlayerPosUnderMap(PoseStack matrixStack, @NotNull Vec3 player, int w, int h, int minimapSize) {
        String pos = String.format("%.2f %.2f %.2f", player.x, player.y, player.z);
        int width = client.font.width(pos);
        int xPos = parent.config.snapSide.right ? w - width - 5 : parent.config.snapSide.center ? w / 2 - width / 2 : 5;
        int yPos = parent.config.snapSide.bottom ? h - client.font.lineHeight - 5 : minimapSize + 10;
        client.font.draw(matrixStack, pos, xPos, yPos, 0xFFFFFF);
    }

    public void renderDebugInfo(PoseStack matrixStack, @NotNull Vec3 player, int w, int h, int minimapSize) {
        int chunkX = ((int) player.x) >> 4;
        int chunkZ = ((int) player.z) >> 4;

        try {
            MapRegion region = parent.currentLevel.getRegion(new MapLevel.Pos(chunkX >> 5, chunkZ >> 5));
            ChunkData chunk = region.data[MapRegion.chunkPosToIndex(chunkX, chunkZ)];
            if (chunk != null) {
                String[] debugInfo = {
                    chunk.resources.get(chunk.blockid[ChunkData.blockPosToIndex(new BlockPos(player))]).toString(), // block
                    chunk.resources.get(chunk.biomeid[ChunkData.blockPosToIndex(new BlockPos(player))]).toString(), // biome
                    String.format("%08x", TestRenderStrategy.getMainTopColor(chunk.resources.get(chunk.blockid[ChunkData.blockPosToIndex(new BlockPos(player))]))), // block-color
                };
                for (int i = 0; i < debugInfo.length; ++i) {
                    int width = client.font.width(debugInfo[i]);
                    int xPos = parent.config.snapSide.right ? w - width - 5 : parent.config.snapSide.center ? w / 2 - width / 2 : 5;
                    int yPos = parent.config.snapSide.bottom ? h - minimapSize - (client.font.lineHeight * (i + 1)) - 10 : minimapSize + 10 + client.font.lineHeight * (i + 1);

                    client.font.draw(matrixStack, debugInfo[i], xPos, yPos, 0xFFFFFF);
                }
            }
        } catch (ExecutionException ignored) {}
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
