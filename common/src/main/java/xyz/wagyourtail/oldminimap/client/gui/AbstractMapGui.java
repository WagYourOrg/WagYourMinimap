package xyz.wagyourtail.oldminimap.client.gui;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import xyz.wagyourtail.oldminimap.client.WagYourMinimapClient;
import xyz.wagyourtail.oldminimap.client.gui.renderer.AbstractMapRenderer;

import java.lang.reflect.InvocationTargetException;

public abstract class AbstractMapGui {
    protected final Minecraft client = Minecraft.getInstance();
    public final WagYourMinimapClient parent;
    public AbstractMapRenderer renderer;
    public AbstractMapGui(WagYourMinimapClient parent) {
        this.parent = parent;
    }

    public void setRenderer(Class<? extends AbstractMapRenderer> renderer) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        this.renderer = renderer.getConstructor(AbstractMapGui.class).newInstance(this);
    }

    abstract public void render(PoseStack matrixStack, float tickDelta);
}
