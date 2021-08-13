package xyz.wagyourtail.minimap.client.gui.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.client.gui.AbstractMapGui;
import xyz.wagyourtail.minimap.client.gui.ThreadsafeDynamicTexture;
import xyz.wagyourtail.minimap.client.gui.image.AbstractImageStrategy;
import xyz.wagyourtail.minimap.client.gui.image.BlockLightImageStrategy;
import xyz.wagyourtail.minimap.client.gui.image.VanillaMapImageStrategy;
import xyz.wagyourtail.minimap.client.gui.renderer.overlay.AbstractMapOverlayRenderer;
import xyz.wagyourtail.minimap.scanner.ChunkData;
import xyz.wagyourtail.minimap.scanner.MapLevel;
import xyz.wagyourtail.minimap.scanner.MapRegion;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public abstract class AbstractMapRenderer {
    protected static final Minecraft minecraft = Minecraft.getInstance();
    public final AbstractMapGui parent;
    public AbstractMapOverlayRenderer[] overlays = new AbstractMapOverlayRenderer[0];

    protected AbstractMapRenderer(AbstractMapGui parent) {
        this.parent = parent;
    }

    private AbstractImageStrategy[] rendererLayers = new AbstractImageStrategy[] {new VanillaMapImageStrategy(), new BlockLightImageStrategy()};

    public void setRenderLayers(AbstractImageStrategy... strategy) {
        this.rendererLayers = strategy;
    }

    public AbstractImageStrategy[] getRenderLayers() {
        return rendererLayers;
    }

    public void setOverlays(AbstractMapOverlayRenderer... overlays) {
        this.overlays = overlays;
    }

    public abstract void renderMinimap(PoseStack matrixStack, @NotNull Vec3 center, float maxLength, @NotNull Vec3 player_pos, float player_rot);

    public abstract void renderText(PoseStack matrixStack, float maxLength, Component... textLines);

    private static boolean bindChunkTex(ChunkData chunkData, AbstractImageStrategy renderer) {
        ThreadsafeDynamicTexture image;
        try {
            image = renderer.getImage(chunkData).resolveAsync(0);
            if (image == null) return false;
            image.bind();
            RenderSystem.setShaderTexture(0, image.getId());
            return true;
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean drawChunk(PoseStack matrixStack, ChunkData chunk, float x, float y, float width, float height, float startU, float startV, float endU, float endV) {
        for (AbstractImageStrategy rendererLayer : rendererLayers) {
            if (!rendererLayer.shouldRender()) continue;
            if (!bindChunkTex(chunk, rendererLayer)) return false;
            drawTex(matrixStack, x, y, width, height, startU, startV, endU, endV);
        }
        return true;
    }

    public void drawTex(PoseStack matrixStack, float x, float y, float width, float height, float startU, float startV, float endU, float endV) {
        Matrix4f matrix = matrixStack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableTexture();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(matrix, x, y + height, 0).uv(startU, endV).endVertex();
        builder.vertex(matrix, x + width, y + height, 0).uv(endU, endV).endVertex();
        builder.vertex(matrix, x + width, y, 0).uv(endU, startV).endVertex();
        builder.vertex(matrix, x, y, 0).uv(startU, startV).endVertex();
        builder.end();
        BufferUploader.end(builder);
    }

    public void drawTexCol(PoseStack matrixStack, float x, float y, float width, float height, float startU, float startV, float endU, float endV, int abgrTint) {
        Matrix4f matrix = matrixStack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableTexture();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        float a = (abgrTint >> 0x18 & 0xFF) / 255f;
        float b = (abgrTint >> 0x10 & 0xFF) / 255f;
        float g = (abgrTint >> 0x08 & 0xFF) / 255f;
        float r = (abgrTint & 0xFF) / 255f;
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        builder.vertex(matrix, x, y + height, 0).uv(startU, endV).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x + width, y + height, 0).uv(endU, endV).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x + width, y, 0).uv(endU, startV).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x, y, 0).uv(startU, startV).color(r, g, b, a).endVertex();
        builder.end();
        BufferUploader.end(builder);
    }

    private void drawEmptyChunk(PoseStack matrixStack, float x, float y, float width, float height) {
        Matrix4f matrix = matrixStack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        builder.vertex(matrix, x, y + height, 0).color(0, 0, 0, 1f).endVertex();
        builder.vertex(matrix, x + width, y + height, 0).color(0, 0, 0, 1f).endVertex();
        builder.vertex(matrix, x + width, y, 0).color(0, 0, 0, 1f).endVertex();
        builder.vertex(matrix, x, y, 0).color(0, 0, 0, 1f).endVertex();
        builder.end();
        BufferUploader.end(builder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    protected ChunkData getChunk(int chunkX, int chunkZ) {
        try {
            MapLevel level = MinimapClientApi.getInstance().getCurrentLevel();
            if (level == null) return null;
            MapRegion region = level.getRegion(new MapLevel.Pos(chunkX >> 5, chunkZ >> 5));
            if (region.data[MapRegion.chunkPosToIndex(chunkX, chunkZ)] == null) return null;
            return region.data[MapRegion.chunkPosToIndex(chunkX, chunkZ)].resolveAsync(0);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void drawPartialChunk(PoseStack stack, ChunkData chunk, float x, float y, float scale, float startBlockX, float startBlockZ, float endBlockX, float endBlockZ) {
        float startX = startBlockX / 16F;
        float startZ = startBlockZ / 16F;
        float endX = endBlockX / 16F;
        float endZ = endBlockZ / 16F;
        float scaledScaleX = scale * (endX - startX);
        float scaledScaleZ = scale * (endZ - startZ);
        if (chunk != null) {
            if (drawChunk(stack, chunk, x, y, scaledScaleX, scaledScaleZ, startX, startZ, endX, endZ)) return;
        }
        drawEmptyChunk(stack, x, y, scaledScaleX, scaledScaleZ);
    }

    protected void drawChunk(PoseStack matrixStack, ChunkData chunk, float x, float y, float scale) {
        if (chunk != null) {
            if (drawChunk(matrixStack, chunk, x, y, scale, scale, 0, 0, 1, 1)) return;
        }
        drawEmptyChunk(matrixStack, x, y, scale, scale);
    }
}
