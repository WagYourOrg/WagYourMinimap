package xyz.wagyourtail.minimap.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import xyz.wagyourtail.minimap.client.gui.renderer.AbstractMapRenderer;

public abstract class AbstractMapGui {
    protected final Minecraft client = Minecraft.getInstance();
    protected AbstractMapRenderer renderer;

    public AbstractMapRenderer getRenderer() {
        return renderer;
    }

    public void setRenderer(AbstractMapRenderer renderer) {
        this.renderer = renderer;
    }

    abstract public void render(PoseStack matrixStack, float tickDelta);

}
