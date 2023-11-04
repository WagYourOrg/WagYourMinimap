package xyz.wagyourtail.minimap.client.gui.hud.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import xyz.wagyourtail.minimap.client.gui.hud.map.AbstractMinimapRenderer;

public abstract class AbstractMinimapOverlay {
    protected static final Minecraft minecraft = Minecraft.getInstance();
    public final AbstractMinimapRenderer parent;

    protected AbstractMinimapOverlay(AbstractMinimapRenderer parent) {
        this.parent = parent;
    }

    public abstract void renderOverlay(GuiGraphics stack, @NotNull Vec3 center, float maxLength, @NotNull Vec3 player_pos, float player_rot);

}
