package xyz.wagyourtail.minimap.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import xyz.wagyourtail.minimap.client.WagYourMinimapClient;
import xyz.wagyourtail.minimap.client.gui.renderer.AbstractRenderStrategy;
import xyz.wagyourtail.minimap.client.gui.renderer.VanillaMapRenderStrategy;
import xyz.wagyourtail.minimap.scanner.ChunkData;

import java.util.concurrent.ExecutionException;

public abstract class AbstractMapGui {
    protected final Minecraft client = Minecraft.getInstance();
    protected final WagYourMinimapClient parent;

    private AbstractRenderStrategy[] rendererLayers = new AbstractRenderStrategy[] {new VanillaMapRenderStrategy()};

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

    private void drawChunk(PoseStack matrixStack, ChunkData chunk, int x, int y, int width, int height) {
        for (AbstractRenderStrategy rendererLayer : rendererLayers) {
            if (!rendererLayer.shouldRender()) continue;
            bindChunkTex(chunk, rendererLayer);
            Matrix4f matrix = matrixStack.last().pose();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            BufferBuilder builder = Tesselator.getInstance().getBuilder();
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            builder.vertex(matrix, x, y + height, 0).uv(0, 1).endVertex();
            builder.vertex(matrix, x + width, y + height, 0).uv(1, 1).endVertex();
            builder.vertex(matrix, x + width, y, 0).uv(1, 0).endVertex();
            builder.vertex(matrix, x, y, 0).uv(0, 0).endVertex();
            builder.end();
            BufferUploader.end(builder);
        }
    }

    public void drawChunk(PoseStack matrixStack, ChunkData chunk, int x, int y, int scale) {
        if (chunk != null) {
            drawChunk(matrixStack, chunk, x, y, scale, scale);
        } else {
            GuiComponent.fill(matrixStack, x, y, x + scale, y + scale, 0xFF000000);
        }
    }

    abstract public void render(PoseStack matrixStack, float tickDelta);
}
