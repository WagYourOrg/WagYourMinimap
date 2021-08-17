package xyz.wagyourtail.minimap.client.gui.renderer.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import xyz.wagyourtail.minimap.client.gui.renderer.AbstractMapRenderer;

public abstract class AbstractMapOverlayRenderer {
    public final AbstractMapRenderer parent;

    protected AbstractMapOverlayRenderer(AbstractMapRenderer parent) {
        this.parent = parent;
    }

    public abstract void renderOverlay(PoseStack stack, @NotNull Vec3 center, float maxLength, @NotNull Vec3 player_pos, float player_rot);

}
