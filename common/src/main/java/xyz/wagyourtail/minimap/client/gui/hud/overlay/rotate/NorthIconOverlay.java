package xyz.wagyourtail.minimap.client.gui.hud.overlay.rotate;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.WagYourMinimap;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.api.client.config.MinimapClientConfig;
import xyz.wagyourtail.minimap.client.gui.AbstractMapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.map.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.overlay.AbstractMinimapOverlay;

@SettingsContainer("gui.wagyourminimap.settings.overlay.north_icon")
public class NorthIconOverlay extends AbstractMinimapOverlay {
    private static final ResourceLocation north_tex = new ResourceLocation(
        WagYourMinimap.MOD_ID,
        "textures/north_icon.png"
    );

    public NorthIconOverlay(AbstractMinimapRenderer parent) {
        super(parent);
    }

    @Override
    public void renderOverlay(GuiGraphics stack, @NotNull Vec3 center, float maxLength, @NotNull Vec3 player_pos, float player_rot) {
        int chunkRadius = MinimapClientApi.getInstance().getConfig().get(MinimapClientConfig.class).chunkRadius;
        int chunkDiam = chunkRadius * 2 - 1;
        float chunkScale = maxLength / ((float) chunkDiam - 1);

        Vec3 pointVec = new Vec3(0, 0, 1).yRot((float) Math.toRadians(player_rot));
        float scale = parent.getScaleForVecToBorder(pointVec, chunkRadius, maxLength);
        pointVec = pointVec.multiply(scale, 1, scale);
        stack.pose().translate(
            maxLength / 2 + pointVec.x * chunkScale / 16f,
            maxLength / 2 + pointVec.z * chunkScale / 16f,
            0
        );
        stack.pose().scale(.005f * maxLength, .005f * maxLength, 1);
        RenderSystem.setShaderTexture(0, north_tex);
        AbstractMapRenderer.drawTex(stack, -10, -10, 20, 20, 1, 1, 0, 0);
    }

}
