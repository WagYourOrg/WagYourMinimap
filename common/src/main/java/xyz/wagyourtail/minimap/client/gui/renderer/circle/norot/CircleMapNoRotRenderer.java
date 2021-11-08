package xyz.wagyourtail.minimap.client.gui.renderer.circle.norot;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.api.config.MinimapClientConfig;
import xyz.wagyourtail.minimap.client.ModloaderSpecific;
import xyz.wagyourtail.minimap.client.gui.renderer.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.client.gui.renderer.circle.CircleMapBorderOverlay;
import xyz.wagyourtail.minimap.client.gui.renderer.overlay.AbstractMapOverlayRenderer;

public class CircleMapNoRotRenderer extends AbstractMinimapRenderer {

    public CircleMapNoRotRenderer() {
        overlays = new AbstractMapOverlayRenderer[] {new CircleMapBorderOverlay(this)};
    }

    @Override
    public void renderMinimap(PoseStack matrixStack, @NotNull Vec3 center, float maxLength, @NotNull Vec3 player_pos, float player_rot) {
        int chunkRadius = MinimapClientApi.getInstance().getConfig().get(MinimapClientConfig.class).chunkRadius;

        float blockX = (float) (center.x % 16);
        float blockZ = (float) (center.z % 16);
        if (blockX < 0) blockX += 16;
        if (blockZ < 0) blockZ += 16;

        int topChunkX = (((int) Math.floor(center.x)) >> 4) - chunkRadius + 1;
        int topChunkZ = (((int) Math.floor(center.z)) >> 4) - chunkRadius + 1;

        if (chunkRadius == 1) {
            topChunkX -= 1;
            topChunkZ -= 1;
        }

        int chunkDiam = chunkRadius * 2 - 1;
        float chunkScale = maxLength / ((float) chunkDiam - 1);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        Vec3 offset = center.subtract(player_pos);

        ModloaderSpecific.instance.checkEnableStencil();

        GL11.glEnable(GL11.GL_STENCIL_TEST);
        RenderSystem.colorMask(false, false, false, false);
        RenderSystem.depthMask(false);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        RenderSystem.stencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        RenderSystem.stencilMask(0xFF);
        RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, false);
        RenderSystem.disableBlend();
        circle(matrixStack, maxLength / 2, maxLength / 2,maxLength / 2, 50);
        RenderSystem.enableBlend();
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthMask(true);
        RenderSystem.stencilMask(0x00);
        RenderSystem.stencilFunc(GL11.GL_EQUAL, 1, 0xFF);

        int i = 0;
        int j = 0;
        float partialX = blockX / 16f * chunkScale;
        float partialZ = blockZ / 16f * chunkScale;

        //DRAW CHUNKS
        drawPartialChunk(matrixStack, getChunk(topChunkX + i, topChunkZ + j), chunkScale * i, chunkScale * j, chunkScale, blockX, blockZ, 16, 16);
        for (++j; j < chunkDiam - 1; ++j) {
            drawPartialChunk(matrixStack, getChunk(topChunkX + i, topChunkZ + j), chunkScale * i, chunkScale * j - partialZ, chunkScale, blockX, 0, 16, 16);
        }
        drawPartialChunk(matrixStack, getChunk(topChunkX + i, topChunkZ + j), chunkScale * i, chunkScale * j - partialZ, chunkScale, blockX, 0, 16, blockZ);
        for (++i; i < chunkDiam - 1; ++i) {
            j = 0;
            drawPartialChunk(matrixStack, getChunk(topChunkX + i, topChunkZ + j), chunkScale * i - partialX, chunkScale * j, chunkScale, 0, blockZ, 16, 16);
            for (++j; j < chunkDiam - 1; ++j) {
                drawChunk(matrixStack, getChunk(topChunkX + i, topChunkZ + j), chunkScale * i - partialX, chunkScale * j - partialZ, chunkScale);
            }
            drawPartialChunk(matrixStack, getChunk(topChunkX + i, topChunkZ + j), chunkScale * i - partialX, chunkScale * j - partialZ, chunkScale, 0, 0, 16, blockZ);
        }
        j = 0;
        drawPartialChunk(matrixStack, getChunk(topChunkX + i, topChunkZ + j), chunkScale * i - partialX, chunkScale * j, chunkScale, 0, blockZ, blockX, 16);
        for (++j; j < chunkDiam - 1; ++j) {
            drawPartialChunk(matrixStack, getChunk(topChunkX + i, topChunkZ + j), chunkScale * i - partialX, chunkScale * j - partialZ, chunkScale, 0, 0, blockX, 16);
        }
        drawPartialChunk(matrixStack, getChunk(topChunkX + i, topChunkZ + j), chunkScale * i - partialX, chunkScale * j - partialZ, chunkScale, 0, 0, blockX, blockZ);

        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }

    public void circle(PoseStack matrixStack, float x, float y, float radius, int segments) {
        matrixStack.translate(x, y, 0);
        Matrix4f matrix = matrixStack.last().pose();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        float dAngle = (float) (2 * Math.PI / segments);
        builder.vertex(matrix, 0, 0, 0).color(0f,0f,0f,1f).endVertex();
        builder.vertex(matrix, radius, 0, 0).color(0f,0f,0f,1f).endVertex();
        float currentAngle = -dAngle;
        for (int i = 1; i < segments; i++) {
            builder.vertex(matrix, (float) Math.cos(currentAngle) * radius, (float) Math.sin(currentAngle) * radius, 0).color(0f,0f,0f,1f).endVertex();
            currentAngle -= dAngle;
        }
        builder.vertex(matrix, radius, 0, 0).color(0f,0f,0f,1f).endVertex();
        builder.end();
        BufferUploader.end(builder);
        matrixStack.translate(-x, -y, 0);
    }

    @Override
    public void renderText(PoseStack matrixStack, float maxLength, boolean bottom, Component... textLines) {
        matrixStack.translate(0, 10, 0);
        float lineOffset = 0;
        for (Component textLine : textLines) {
            int len = minecraft.font.width(textLine);
            float scale = len / maxLength;
            if (scale > 1) {
                matrixStack.scale(1 / scale, 1 / scale, 0);
            }
            minecraft.font.drawShadow(matrixStack, textLine, len < maxLength ? (maxLength - len) / 2 : 0, lineOffset, 0xFFFFFF);
            if (scale > 1) {
                matrixStack.scale(scale, scale, 0);
                lineOffset += scale * minecraft.font.lineHeight;
            } else {
                lineOffset += minecraft.font.lineHeight;
            }
        }
    }
}
