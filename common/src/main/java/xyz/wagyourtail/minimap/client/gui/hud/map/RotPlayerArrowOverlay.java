package xyz.wagyourtail.minimap.client.gui.hud.map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import xyz.wagyourtail.minimap.WagYourMinimap;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.api.config.MinimapClientConfig;
import xyz.wagyourtail.minimap.client.gui.AbstractMapRenderer;

public class RotPlayerArrowOverlay extends AbstractMapOverlayRenderer {
    private static final ResourceLocation player_icon_tex = new ResourceLocation(WagYourMinimap.MOD_ID, "textures/player_arrow.png");

    public RotPlayerArrowOverlay(AbstractMinimapRenderer parent) {
        super(parent);
    }

    @Override
    public void renderOverlay(PoseStack stack, @NotNull Vec3 center, float maxLength, @NotNull Vec3 player_pos, float player_rot) {
        int chunkRadius = MinimapClientApi.getInstance().getConfig().get(MinimapClientConfig.class).chunkRadius;

        int chunkDiam = chunkRadius * 2 - 1;
        float chunkScale = maxLength / ((float) chunkDiam - 1);

        Vec3 offset = center.subtract(player_pos);
        stack.translate(maxLength / 2, maxLength / 2, 0);
        stack.mulPose(Vector3f.ZN.rotationDegrees(player_rot - 180));
        stack.translate(-maxLength / 2, -maxLength / 2, 0);

        stack.translate(maxLength / 2 + offset.x * chunkScale / 16f, maxLength / 2 + offset.z * chunkScale / 16f, 0);
        stack.mulPose(Vector3f.ZP.rotationDegrees(player_rot));
//        stack.mulPose(Vector3f.ZP.rotationDegrees(player_rot));

        RenderSystem.setShaderTexture(0, player_icon_tex);
        float texSize = Math.max(maxLength / 20, 8);
        AbstractMapRenderer.drawTexCol(stack, -texSize, -texSize, texSize * 2, texSize * 2, 1, 1, 0, 0, 0xFF0000FF);
    }

}
