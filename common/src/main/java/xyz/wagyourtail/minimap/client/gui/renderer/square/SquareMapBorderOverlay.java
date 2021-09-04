package xyz.wagyourtail.minimap.client.gui.renderer.square;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import xyz.wagyourtail.minimap.WagYourMinimap;
import xyz.wagyourtail.minimap.client.gui.renderer.AbstractMapRenderer;
import xyz.wagyourtail.minimap.client.gui.renderer.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.client.gui.renderer.overlay.AbstractMapOverlayRenderer;

public class SquareMapBorderOverlay extends AbstractMapOverlayRenderer {
    private static final ResourceLocation map_corner = new ResourceLocation(WagYourMinimap.MOD_ID, "textures/square_border_corner.png");
    private static final ResourceLocation map_side = new ResourceLocation(WagYourMinimap.MOD_ID, "textures/square_border_side.png");
    public SquareMapBorderOverlay(AbstractMinimapRenderer parent) {
        super(parent);
    }

    @Override
    public void renderOverlay(PoseStack stack, @NotNull Vec3 center, float maxLength, @NotNull Vec3 player_pos, float player_rot) {
        int texcount = Math.max((int) Math.ceil(maxLength / 32), 2);
        float texLength = (maxLength + 32) / texcount;
        // top left
        RenderSystem.setShaderTexture(0, map_corner);
        AbstractMapRenderer.drawTex(stack, -16, -16, texLength, texLength, 0, 1, 1, 0);
        // bottom left
        AbstractMapRenderer.drawTex(stack, -16, -16 + texLength * (texcount - 1), texLength, texLength, 0, 0, 1, 1);
        // top right
        AbstractMapRenderer.drawTex(stack, -16 + texLength * (texcount - 1), -16, texLength, texLength, 1, 1, 0, 0);
        // bottom right
        AbstractMapRenderer.drawTex(stack, -16 + texLength * (texcount - 1), -16 + texLength * (texcount - 1), texLength, texLength, 1, 0, 0, 1);
        RenderSystem.setShaderTexture(0, map_side);
        for (int i = 2; i < texcount; ++i) {
            AbstractMapRenderer.drawTex(stack, -16 + texLength * (i - 1), -16, texLength, texLength, 0, 1, 1, 0);
            AbstractMapRenderer.drawTex(stack, -16 + texLength * (i - 1), -16 + texLength * (texcount - 1), texLength, texLength, 0, 0, 1, 1);
        }
        for (int i = 2; i < texcount; ++i) {
            AbstractMapRenderer.drawTexSideways(stack, -16, -16 + texLength * (i - 1), texLength, texLength, 0, 0, 1, 1);
            AbstractMapRenderer.drawTexSideways(stack, -16 + texLength * (texcount - 1), -16 + texLength * (i - 1), texLength, texLength, 1, 1, 0, 0);
        }
    }

}
