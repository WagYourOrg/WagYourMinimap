package xyz.wagyourtail.minimap.client.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import xyz.wagyourtail.minimap.client.WagYourMinimapClient;

public abstract class AbstractMapRenderer {
    protected final Minecraft client = Minecraft.getInstance();
    protected final WagYourMinimapClient parent;

    public AbstractMapRenderer(WagYourMinimapClient parent) {
        this.parent = parent;
    }

    abstract public void render(PoseStack matrixStack, float tickDelta);
}
