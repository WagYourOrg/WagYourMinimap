package xyz.wagyourtail.minimap.client.gui.hud.map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import xyz.wagyourtail.minimap.ModLoaderSpecific;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.api.client.config.MinimapClientConfig;
import xyz.wagyourtail.minimap.client.gui.AbstractMapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.overlay.AbstractMinimapOverlay;

public abstract class AbstractMinimapRenderer extends AbstractMapRenderer {
    public final boolean rotate;
    public final float scaleBy;
    public final boolean hasStencil;
    public AbstractMinimapOverlay[] overlays = new AbstractMinimapOverlay[0];

    protected AbstractMinimapRenderer(boolean rotate, float scaleBy, boolean hasStencil) {
        this.rotate = rotate;
        this.scaleBy = scaleBy;
        this.hasStencil = hasStencil;
    }

    public void setOverlays(AbstractMinimapOverlay... overlays) {
        this.overlays = overlays;
    }

    public void render(PoseStack matrixStack, float tickDelta) {
        if (minecraft.options.renderDebug) {
            return;
        }
        matrixStack.pushPose();
        int w = minecraft.getWindow().getGuiScaledWidth();
        int h = minecraft.getWindow().getGuiScaledHeight();

        float minimapSize = Math.min(w, h) *
            MinimapClientApi.getInstance().getConfig().get(MinimapClientConfig.class).minimapScale / 100f;

        LocalPlayer player = minecraft.player;
        assert player != null;

        boolean bottom = MinimapClientApi.getInstance().getConfig().get(MinimapClientConfig.class).snapSide.bottom;

        float posX = MinimapClientApi.getInstance().getConfig().get(MinimapClientConfig.class).snapSide.right ?
            w - minimapSize - 10 :
            MinimapClientApi.getInstance().getConfig().get(MinimapClientConfig.class).snapSide.center ?
                w / 2f - minimapSize / 2f :
                10;
        float posZ = bottom ? h - minimapSize - minecraft.font.lineHeight - 10 : 10;
        Vec3 player_pos = player.getPosition(tickDelta);
        float player_rot = player.getYRot();

        //pull back map to 0, 0
        matrixStack.translate(posX, posZ, 0);
        matrixStack.pushPose();
        renderMinimap(matrixStack, player_pos, minimapSize, player_pos, player_rot);
        matrixStack.popPose();

        //DRAW OVERLAYS
        for (AbstractMinimapOverlay overlay : overlays) {
            matrixStack.pushPose();
            overlay.renderOverlay(matrixStack, player_pos, minimapSize, player_pos, player_rot);
            matrixStack.popPose();
        }
        matrixStack.popPose();

        //pull back text pos to 0, 0
        matrixStack.pushPose();
        matrixStack.translate(posX, posZ, 0);
        if (!bottom) {
            matrixStack.translate(0, minimapSize + 5, 0);
        }
        renderText(
            matrixStack,
            minimapSize,
            bottom,
            new TextComponent(String.format("%.2f %.2f %.2f", player_pos.x, player_pos.y, player_pos.z))
        );
        matrixStack.popPose();
    }

    protected void renderMinimap(PoseStack matrixStack, @NotNull Vec3 center, float maxLength, @NotNull Vec3 player_pos, float player_rot) {
        int chunkRadius = MinimapClientApi.getInstance().getConfig().get(MinimapClientConfig.class).chunkRadius;

        float blockX = (float) (center.x % 16);
        float blockZ = (float) (center.z % 16);
        if (blockX < 0) {
            blockX += 16;
        }
        if (blockZ < 0) {
            blockZ += 16;
        }

        int topChunkX = (((int) Math.floor(center.x)) >> 4) - chunkRadius + 1;
        int topChunkZ = (((int) Math.floor(center.z)) >> 4) - chunkRadius + 1;

        if (chunkRadius == 1) {
            topChunkX -= 1;
            topChunkZ -= 1;
        }

        int chunkDiam = chunkRadius * 2 - 1;
        float chunkScale = maxLength / ((float) chunkDiam - 1);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        if (hasStencil) {
            ModLoaderSpecific.checkEnableStencil();

            GL11.glEnable(GL11.GL_STENCIL_TEST);
            RenderSystem.colorMask(false, false, false, false);
            RenderSystem.depthMask(false);
            RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
            RenderSystem.stencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
            RenderSystem.stencilMask(0xFF);
            RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, false);
            RenderSystem.disableBlend();
            drawStencil(matrixStack, maxLength);
            RenderSystem.enableBlend();
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.depthMask(true);
            RenderSystem.stencilMask(0x00);
            RenderSystem.stencilFunc(GL11.GL_EQUAL, 1, 0xFF);
        }

        if (rotate) {
            rotateMatrix(matrixStack, maxLength, player_rot);
        }
        if (scaleBy != 1) {
            scaleMatrix(matrixStack, maxLength, scaleBy);
        }

        int i = 0;
        int j = 0;
        float partialX = blockX / 16f * chunkScale;
        float partialZ = blockZ / 16f * chunkScale;

        //DRAW CHUNKS
        drawPartialChunk(
            matrixStack,
            getChunk(topChunkX + i, topChunkZ + j),
            chunkScale * i,
            chunkScale * j,
            chunkScale,
            blockX,
            blockZ,
            16,
            16
        );
        for (++j; j < chunkDiam - 1; ++j) {
            drawPartialChunk(
                matrixStack,
                getChunk(topChunkX + i, topChunkZ + j),
                chunkScale * i,
                chunkScale * j - partialZ,
                chunkScale,
                blockX,
                0,
                16,
                16
            );
        }
        drawPartialChunk(
            matrixStack,
            getChunk(topChunkX + i, topChunkZ + j),
            chunkScale * i,
            chunkScale * j - partialZ,
            chunkScale,
            blockX,
            0,
            16,
            blockZ
        );
        for (++i; i < chunkDiam - 1; ++i) {
            j = 0;
            drawPartialChunk(
                matrixStack,
                getChunk(topChunkX + i, topChunkZ + j),
                chunkScale * i - partialX,
                chunkScale * j,
                chunkScale,
                0,
                blockZ,
                16,
                16
            );
            for (++j; j < chunkDiam - 1; ++j) {
                drawChunk(
                    matrixStack,
                    getChunk(topChunkX + i, topChunkZ + j),
                    chunkScale * i - partialX,
                    chunkScale * j - partialZ,
                    chunkScale
                );
            }
            drawPartialChunk(
                matrixStack,
                getChunk(topChunkX + i, topChunkZ + j),
                chunkScale * i - partialX,
                chunkScale * j - partialZ,
                chunkScale,
                0,
                0,
                16,
                blockZ
            );
        }
        j = 0;
        drawPartialChunk(
            matrixStack,
            getChunk(topChunkX + i, topChunkZ + j),
            chunkScale * i - partialX,
            chunkScale * j,
            chunkScale,
            0,
            blockZ,
            blockX,
            16
        );
        for (++j; j < chunkDiam - 1; ++j) {
            drawPartialChunk(
                matrixStack,
                getChunk(topChunkX + i, topChunkZ + j),
                chunkScale * i - partialX,
                chunkScale * j - partialZ,
                chunkScale,
                0,
                0,
                blockX,
                16
            );
        }
        drawPartialChunk(
            matrixStack,
            getChunk(topChunkX + i, topChunkZ + j),
            chunkScale * i - partialX,
            chunkScale * j - partialZ,
            chunkScale,
            0,
            0,
            blockX,
            blockZ
        );
        if (hasStencil) {
            GL11.glDisable(GL11.GL_STENCIL_TEST);
        }
    }

    public void rotateMatrix(PoseStack matrixStack, float maxLength, float player_rot) {
        matrixStack.translate(maxLength / 2, maxLength / 2, 0);
        matrixStack.mulPose(Vector3f.ZN.rotationDegrees(player_rot - 180));
        matrixStack.translate(-maxLength / 2, -maxLength / 2, 0);
    }

    public void scaleMatrix(PoseStack matrixStack, float maxLength, float scaleBy) {
        matrixStack.translate(maxLength / 2, maxLength / 2, 0);
        matrixStack.scale(scaleBy, scaleBy, 1);
        matrixStack.translate(-maxLength / 2, -maxLength / 2, 0);
    }

    protected abstract void drawStencil(PoseStack stack, float maxLength);

    public void renderText(PoseStack matrixStack, float maxLength, boolean bottom, Component... textLines) {
        matrixStack.translate(0, 10, 0);
        float lineOffset = 0;
        for (Component textLine : textLines) {
            int len = minecraft.font.width(textLine);
            float scale = len / maxLength;
            if (scale > 1) {
                matrixStack.scale(1 / scale, 1 / scale, 0);
            }
            minecraft.font.drawShadow(
                matrixStack,
                textLine,
                len < maxLength ? (maxLength - len) / 2 : 0,
                lineOffset,
                0xFFFFFF
            );
            if (scale > 1) {
                matrixStack.scale(scale, scale, 0);
                lineOffset += scale * minecraft.font.lineHeight;
            } else {
                lineOffset += minecraft.font.lineHeight;
            }
        }
    }

    public abstract float getScaleForVecToBorder(Vec3 in, int chunkRadius, float maxLength);

}
