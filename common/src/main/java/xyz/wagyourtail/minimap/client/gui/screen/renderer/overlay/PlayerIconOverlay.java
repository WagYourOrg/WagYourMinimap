package xyz.wagyourtail.minimap.client.gui.screen.renderer.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.world.phys.Vec3;
import xyz.wagyourtail.minimap.client.gui.screen.renderer.AbstractFullscreenOverlay;
import xyz.wagyourtail.minimap.client.gui.screen.renderer.ScreenMapRenderer;

public class PlayerIconOverlay extends AbstractFullscreenOverlay {
    public PlayerIconOverlay(ScreenMapRenderer parent) {
        super(parent);
    }

    @Override
    public void renderOverlay(PoseStack stack) {
        float topX = parent.topX;
        float topZ = parent.topZ;
        float endX = parent.topX + parent.xDiam;
        float endZ = parent.topZ + parent.zDiam;

        Vec3 pos = minecraft.player.position();

        if (pos.x > topX && pos.x < endX && pos.z > topZ && pos.z < endZ) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderTexture(0, minecraft.player.getSkinTextureLocation());
            float posX = (float) (pos.x - parent.topX) * parent.chunkWidth / 16f;
            float posZ = (float) (pos.z - parent.topZ) * parent.chunkWidth / 16f;
            stack.pushPose();
            stack.translate(posX, posZ, 0);
            GuiComponent.blit(stack, -4,-4, 8, 8, 8.0F, 8, 8, 8, 64, 64);
            stack.popPose();
        }

    }

}
