package xyz.wagyourtail.minimap.client.gui.screen.map;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;

public abstract class AbstractFullscreenOverlay {
    public static final Minecraft minecraft = Minecraft.getInstance();
    protected ScreenMapRenderer parent;

    public AbstractFullscreenOverlay(ScreenMapRenderer parent) {
        this.parent = parent;
    }

    public abstract void renderOverlay(PoseStack stack, int mouseX, int mouseY);

}
