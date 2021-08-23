package xyz.wagyourtail.minimap.client.gui.renderer.square.norot;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import xyz.wagyourtail.minimap.client.gui.renderer.AbstractMapRenderer;
import xyz.wagyourtail.minimap.client.gui.renderer.overlay.AbstractMapOverlayRenderer;

public class SquareMapNoRotWaypointOverlay extends AbstractMapOverlayRenderer {
    protected SquareMapNoRotWaypointOverlay(AbstractMapRenderer parent) {
        super(parent);
    }

    @Override
    public void renderOverlay(PoseStack stack, @NotNull Vec3 center, float maxLength, @NotNull Vec3 player_pos, float player_rot) {

    }

}
