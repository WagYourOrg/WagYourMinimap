package xyz.wagyourtail.minimap.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import xyz.wagyourtail.minimap.client.WagYourMinimapClient;
import xyz.wagyourtail.minimap.client.gui.renderer.AbstractRenderStrategy;
import xyz.wagyourtail.minimap.client.gui.renderer.BlockLightOverlayStrategy;
import xyz.wagyourtail.minimap.client.gui.renderer.VanillaMapRenderStrategy;
import xyz.wagyourtail.minimap.scanner.ChunkData;
import xyz.wagyourtail.minimap.scanner.MapLevel;
import xyz.wagyourtail.minimap.scanner.MapRegion;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public abstract class AbstractMapGui {
    protected final Minecraft client = Minecraft.getInstance();
    protected final WagYourMinimapClient parent;

    private AbstractRenderStrategy[] rendererLayers = new AbstractRenderStrategy[] {new VanillaMapRenderStrategy(), new BlockLightOverlayStrategy()};

    public AbstractMapGui(WagYourMinimapClient parent) {
        this.parent = parent;
    }


    public void setRenderLayers(AbstractRenderStrategy... strategy) {
        this.rendererLayers = strategy;
    }

    public AbstractRenderStrategy[] getRenderLayers() {
        return rendererLayers;
    }

    private static void bindChunkTex(ChunkData chunkData, AbstractRenderStrategy renderer) {
        DynamicTexture image;
        try {
            image = renderer.getImage(chunkData);
            image.bind();
            RenderSystem.setShaderTexture(0, image.getId());
        } catch (ExecutionException ignored) {}
    }

    private void drawChunk(PoseStack matrixStack, ChunkData chunk, float x, float y, float width, float height, float startU, float startV, float endU, float endV) {
        for (AbstractRenderStrategy rendererLayer : rendererLayers) {
            if (!rendererLayer.shouldRender()) continue;
            bindChunkTex(chunk, rendererLayer);
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
            MapRegion region = parent.currentLevel.getRegion(new MapLevel.Pos(chunkX >> 5, chunkZ >> 5));
            if (region.data[MapRegion.chunkPosToIndex(chunkX, chunkZ)] == null) return null;
            return region.data[MapRegion.chunkPosToIndex(chunkX, chunkZ)].resolveAsync(1);
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
            drawChunk(stack, chunk, x, y, scaledScaleX, scaledScaleZ, startX, startZ, endX, endZ);
        } else {
            drawEmptyChunk(stack, x, y, scaledScaleX, scaledScaleZ);
        }
    }

    protected void drawChunk(PoseStack matrixStack, ChunkData chunk, float x, float y, float scale) {
        if (chunk != null) {
            drawChunk(matrixStack, chunk, x, y, scale, scale, 0, 0, 1, 1);
        } else {
            drawEmptyChunk(matrixStack, x, y, scale, scale);
        }
    }

    abstract public void render(PoseStack matrixStack, float tickDelta);
}
