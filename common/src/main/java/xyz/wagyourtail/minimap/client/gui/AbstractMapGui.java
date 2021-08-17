package xyz.wagyourtail.minimap.client.gui;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import xyz.wagyourtail.minimap.client.WagYourMinimapClient;
import xyz.wagyourtail.minimap.client.gui.renderer.AbstractMapRenderer;

import java.lang.reflect.InvocationTargetException;

public abstract class AbstractMapGui {
    protected final Minecraft client = Minecraft.getInstance();
    protected AbstractMapRenderer renderer;

    public void setRenderer(AbstractMapRenderer renderer) {
        this.renderer = renderer;
    }

    public AbstractMapRenderer getRenderer() {
        return renderer;
    }

    abstract public void render(PoseStack matrixStack, float tickDelta);
}
