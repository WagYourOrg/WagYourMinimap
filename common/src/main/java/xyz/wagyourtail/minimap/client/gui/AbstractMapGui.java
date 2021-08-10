package xyz.wagyourtail.minimap.client.gui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import xyz.wagyourtail.minimap.client.WagYourMinimapClient;
import xyz.wagyourtail.minimap.client.gui.renderer.AbstractRenderStrategy;
import xyz.wagyourtail.minimap.client.gui.renderer.TestRenderStrategy;
import xyz.wagyourtail.minimap.scanner.ChunkData;

import java.util.concurrent.ExecutionException;

public abstract class AbstractMapGui {
    protected final Minecraft client = Minecraft.getInstance();
    protected final WagYourMinimapClient parent;

    private AbstractRenderStrategy renderer = new TestRenderStrategy();

    public AbstractMapGui(WagYourMinimapClient parent) {
        this.parent = parent;
    }

    public void setRenderer(AbstractRenderStrategy strategy) {
        this.renderer = strategy;
    }

    public AbstractRenderStrategy getRenderer() {
        return renderer;
    }

    public void bindChunkTex(ChunkData chunkData) {
        DynamicTexture image;
        try {
            image = renderer.getImage(chunkData);
            image.bind();
            RenderSystem.setShaderTexture(0, image.getId());
        } catch (ExecutionException ignored) {}
    }

    abstract public void render(PoseStack matrixStack, float tickDelta);
}
