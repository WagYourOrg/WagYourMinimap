package xyz.wagyourtail.minimap.client.gui.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.client.gui.ThreadsafeDynamicTexture;
import xyz.wagyourtail.minimap.client.gui.image.AbstractImageStrategy;
import xyz.wagyourtail.minimap.client.gui.image.BlockLightImageStrategy;
import xyz.wagyourtail.minimap.client.gui.image.VanillaMapImageStrategy;
import xyz.wagyourtail.minimap.map.MapLevel;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkLocation;

import java.util.concurrent.ExecutionException;

public abstract class AbstractMapRenderer {
    public static final Minecraft minecraft = Minecraft.getInstance();
    private AbstractImageStrategy[] rendererLayers = new AbstractImageStrategy[] {new VanillaMapImageStrategy(), new BlockLightImageStrategy()};

    public static void drawTexSideways(PoseStack matrixStack, float x, float y, float width, float height, float startU, float startV, float endU, float endV) {
        Matrix4f matrix = matrixStack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableTexture();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(matrix, x, y + height, 0).uv(startU, endV).endVertex();
        builder.vertex(matrix, x + width, y + height, 0).uv(startU, startV).endVertex();
        builder.vertex(matrix, x + width, y, 0).uv(endU, startV).endVertex();
        builder.vertex(matrix, x, y, 0).uv(endU, endV).endVertex();
        builder.end();
        BufferUploader.end(builder);
    }

    public static void drawTexCol(PoseStack matrixStack, float x, float y, float width, float height, float startU, float startV, float endU, float endV, int abgrTint) {
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

    public AbstractImageStrategy[] getRenderLayers() {
        return rendererLayers;
    }

    public void setRenderLayers(AbstractImageStrategy... strategy) {
        this.rendererLayers = strategy;
    }

    protected ChunkLocation getChunk(int chunkX, int chunkZ) {
        MapLevel level = MinimapClientApi.getInstance().getMapLevel(minecraft.level);
        if (level == null) return null;
        return ChunkLocation.locationForChunkPos(level, chunkX, chunkZ);
    }

    protected void drawPartialChunk(PoseStack stack, ChunkLocation chunk, float x, float y, float scale, float startBlockX, float startBlockZ, float endBlockX, float endBlockZ) {
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

    private boolean drawChunk(PoseStack matrixStack, ChunkLocation chunk, float x, float y, float width, float height, float startU, float startV, float endU, float endV) {
        for (AbstractImageStrategy rendererLayer : rendererLayers) {
            if (!rendererLayer.shouldRender()) continue;
            if (!bindChunkTex(chunk, rendererLayer)) return false;
            drawTex(matrixStack, x, y, width, height, startU, startV, endU, endV);
        }
        return true;
    }

    private static boolean bindChunkTex(ChunkLocation chunkData, AbstractImageStrategy renderer) {
        try {
            ThreadsafeDynamicTexture image = renderer.getImage(chunkData);
            if (image == null) return false;
            RenderSystem.setShaderTexture(0, image.getId());
            return true;
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void drawTex(PoseStack matrixStack, float x, float y, float width, float height, float startU, float startV, float endU, float endV) {
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

    protected void drawChunk(PoseStack matrixStack, ChunkLocation chunk, float x, float y, float scale) {
        if (chunk != null) {
            if (drawChunk(matrixStack, chunk, x, y, scale, scale, 0, 0, 1, 1)) return;
        }
        drawEmptyChunk(matrixStack, x, y, scale, scale);
    }

}
