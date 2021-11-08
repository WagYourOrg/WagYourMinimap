package xyz.wagyourtail.minimap.client.gui.hud.map;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import xyz.wagyourtail.minimap.client.gui.AbstractMapRenderer;

public abstract class AbstractMapOverlayRenderer {
    protected static final Minecraft minecraft = Minecraft.getInstance();
    public final AbstractMapRenderer parent;

    protected AbstractMapOverlayRenderer(AbstractMinimapRenderer parent) {
        this.parent = parent;
    }

    public abstract void renderOverlay(PoseStack stack, @NotNull Vec3 center, float maxLength, @NotNull Vec3 player_pos, float player_rot);

}
