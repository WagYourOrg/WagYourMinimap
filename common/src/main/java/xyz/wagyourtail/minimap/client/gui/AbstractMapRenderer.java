package xyz.wagyourtail.minimap.client.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import xyz.wagyourtail.config.field.Setting;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.common.mixins.BufferUploaderAccessor;
import xyz.wagyourtail.minimap.map.MapServer;
import xyz.wagyourtail.minimap.map.image.AccurateMapImageStrategy;
import xyz.wagyourtail.minimap.map.image.ImageStrategy;
import xyz.wagyourtail.minimap.map.image.SurfaceBlockLightImageStrategy;
import xyz.wagyourtail.minimap.map.image.VanillaMapImageStrategy;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public abstract class AbstractMapRenderer {
    public static final Minecraft minecraft = Minecraft.getInstance();

    public static final Set<Class<? extends ImageStrategy>> availableLayers = new HashSet<>(Set.of(
        SurfaceBlockLightImageStrategy.class
    ));

    public static final Set<Class<? extends ImageStrategy>> baseLayers = new HashSet<>(Set.of(
        VanillaMapImageStrategy.class,
        AccurateMapImageStrategy.class
    ));

    @Setting(value = "gui.wagyourminimap.settings.style.base_layer",
        options = "baseLayerOptions",
        setter = "setBaseLayer")
    public ImageStrategy baseLayer;

    @Setting(value = "gui.wagyourminimap.settings.style.layers", options = "layerOptions", setter = "setRenderLayers")
    public ImageStrategy[] rendererLayers;


    public AbstractMapRenderer() {
        baseLayer = new VanillaMapImageStrategy();
        this.rendererLayers = getDefaultLayers().toArray(new ImageStrategy[0]);
    }

    public List<ImageStrategy> getDefaultLayers() {
        return List.of(new SurfaceBlockLightImageStrategy());
    }

    public static void drawTexSideways(PoseStack matrixStack, float x, float y, float width, float height, float startU, float startV, float endU, float endV) {
        Matrix4f matrix = matrixStack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableTexture();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(matrix, x, y, 0).uv(endU, endV).endVertex();
        builder.vertex(matrix, x, y + height, 0).uv(startU, endV).endVertex();
        builder.vertex(matrix, x + width, y, 0).uv(endU, startV).endVertex();
        builder.vertex(matrix, x + width, y + height, 0).uv(startU, startV).endVertex();
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
        builder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_TEX_COLOR);
        builder.vertex(matrix, x, y, 0).uv(startU, startV).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x, y + height, 0).uv(startU, endV).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x + width, y, 0).uv(endU, startV).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x + width, y + height, 0).uv(endU, endV).color(r, g, b, a).endVertex();
        builder.end();
        BufferUploader.end(builder);
    }

    private static DynamicTexture getChunkTex(ChunkLocation chunk, ImageStrategy renderer) {
        try {
            return renderer.getImage(chunk);
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void drawTex(PoseStack matrixStack, float x, float y, float width, float height, float startU, float startV, float endU, float endV) {
        Matrix4f matrix = matrixStack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableTexture();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(matrix, x, y, 0).uv(startU, startV).endVertex();
        builder.vertex(matrix, x, y + height, 0).uv(startU, endV).endVertex();
        builder.vertex(matrix, x + width, y, 0).uv(endU, startV).endVertex();
        builder.vertex(matrix, x + width, y + height, 0).uv(endU, endV).endVertex();
        builder.end();
        BufferUploader.end(builder);
    }

    public void setRenderLayers(ImageStrategy... strategy) {
        this.rendererLayers = strategy;
    }

    public void setBaseLayer(ImageStrategy strategy) {
        this.baseLayer = strategy;
    }

    protected ChunkLocation getChunk(int chunkX, int chunkZ) {
        assert minecraft.level != null;
        MapServer.MapLevel level = MinimapClientApi.getInstance().getMapServer().getLevelFor(minecraft.level);
        if (level == null) {
            return null;
        }
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
            if (drawChunk(stack, chunk, x, y, scaledScaleX, scaledScaleZ, startX, startZ, endX, endZ)) {
                return;
            }
        }
        rect(stack, x, y, scaledScaleX, scaledScaleZ);
    }

    protected boolean drawChunk(PoseStack matrixStack, ChunkLocation chunk, float x, float y, float width, float height, float startU, float startV, float endU, float endV) {
        return drawChunkBatchTex(matrixStack, x, y, width, height, startU, startV, endU, endV, chunk, getLayers());
    }

    public List<ImageStrategy> getLayers() {
        List<ImageStrategy> a = Lists.newArrayList(baseLayer);
        if (rendererLayers != null) {
            a.addAll(List.of(rendererLayers));
        }
        return a;
    }

    public boolean drawChunkBatchTex(PoseStack matrixStack, float x, float y, float width, float height, float startU, float startV, float endU, float endV, ChunkLocation chunk, List<ImageStrategy> layers) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableTexture();
        boolean ret = false;
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        Matrix4f matrix = matrixStack.last().pose();
        builder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(matrix, x, y, 0).uv(startU, startV).endVertex();
        builder.vertex(matrix, x, y + height, 0).uv(startU, endV).endVertex();
        builder.vertex(matrix, x + width, y, 0).uv(endU, startV).endVertex();
        builder.vertex(matrix, x + width, y + height, 0).uv(endU, endV).endVertex();
        builder.end();
        Pair<BufferBuilder.DrawState, ByteBuffer> p = builder.popNextBuffer();
        if (RenderSystem.isOnRenderThreadOrInit()) {
            BufferBuilder.DrawState drawStatex = p.getFirst();
            for (ImageStrategy layer : layers) {
                if (bindLayer(chunk, layer, 0)) {
                    ret = true;
                    BufferUploaderAccessor.invoke_End(
                        p.getSecond(),
                        drawStatex.mode(),
                        drawStatex.format(),
                        drawStatex.vertexCount(),
                        drawStatex.indexType(),
                        drawStatex.indexCount(),
                        drawStatex.sequentialIndex()
                    );
                }
            }
        } else {
            throw new AssertionError("not implemented");
        }
        return ret;
    }

    protected boolean bindLayer(ChunkLocation chunk, ImageStrategy layer, int index) {
        if (!layer.shouldRender()) {
            return false;
        }
        if (!bindChunkTex(chunk, layer, index)) {
            return false;
        }
        return true;
    }

    private static boolean bindChunkTex(ChunkLocation chunkData, ImageStrategy renderer, int index) {
        try {
            DynamicTexture image = renderer.getImage(chunkData);
            if (image == null) {
                return false;
            }
            RenderSystem.setShaderTexture(index, image.getId());
            return true;
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void rect(PoseStack matrixStack, float x, float y, float width, float height) {
        Matrix4f matrix = matrixStack.last().pose();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        builder.vertex(matrix, x, y, 0).color(0, 0, 0, 1f).endVertex();
        builder.vertex(matrix, x, y + height, 0).color(0, 0, 0, 1f).endVertex();
        builder.vertex(matrix, x + width, y, 0).color(0, 0, 0, 1f).endVertex();
        builder.vertex(matrix, x + width, y + height, 0).color(0, 0, 0, 1f).endVertex();
        builder.end();
        BufferUploader.end(builder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    protected void drawChunk(PoseStack matrixStack, ChunkLocation chunk, float x, float y, float scale) {
        if (chunk != null) {
            if (drawChunk(matrixStack, chunk, x, y, scale, scale, 0, 0, 1, 1)) {
                return;
            }
        }
        rect(matrixStack, x, y, scale, scale);
    }

    public Collection<Class<? extends ImageStrategy>> layerOptions() {
        return availableLayers;
    }

    public Collection<Class<? extends ImageStrategy>> baseLayerOptions() {
        return baseLayers;
    }

}
