package xyz.wagyourtail.minimap.client.gui.hud.overlay.mobicons;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import xyz.wagyourtail.minimap.client.gui.AbstractMapRenderer;

public class PlayerEntityRenderer extends AbstractEntityRenderer<Player> {

    @Override
    public boolean canUseFor(LivingEntity entity) {
        return entity instanceof Player;
    }

    @Override
    public void render(PoseStack stack, LivingEntity entity, float maxSize) {
        if (entity == minecraft.getCameraEntity()) return; // don't render the controlled entity, it's already the arrow
        Player player = (Player) entity;
        PlayerInfo info = minecraft.getConnection().getPlayerInfo(player.getUUID());
        if (info != null) {
            RenderSystem.setShaderTexture(0, info.getSkinLocation());
            stack.translate(-maxSize / 2, -maxSize / 2, 1);
            AbstractMapRenderer.drawTex(stack, 0, 0, maxSize, maxSize, 8/64f, 8/64f, 16/64f, 16/64f);
        }
    }

}
