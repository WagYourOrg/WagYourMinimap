package xyz.wagyourtail.minimap.client.gui.screen.map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec3;
import xyz.wagyourtail.config.field.SettingsContainer;

@SettingsContainer("gui.wagyourminimap.settings.overlay.player_icons")
public class PlayerIconOverlay extends AbstractFullscreenOverlay {
    public PlayerIconOverlay(ScreenMapRenderer parent) {
        super(parent);
    }

    @Override
    public void renderOverlay(GuiGraphics stack, int mouseX, int mouseY) {
        float endX = parent.topX + parent.xDiam;
        float endZ = parent.topZ + parent.zDiam;

        Vec3 pos = minecraft.player.position();

        if (pos.x > parent.topX && pos.x < endX && pos.z > parent.topZ && pos.z < endZ) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderTexture(0, minecraft.player.getSkinTextureLocation());
            float posX = (float) (pos.x - parent.topX) * parent.chunkWidth / 16f;
            float posZ = (float) (pos.z - parent.topZ) * parent.chunkWidth / 16f;
            stack.pose().pushPose();
            stack.pose().translate(posX, posZ, 0);
            stack.blit(minecraft.player.getSkinTextureLocation(), -4, -4, 8, 8, 8.0F, 8, 8, 8, 64, 64);
            stack.pose().popPose();
        }

    }

}
