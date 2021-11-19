package xyz.wagyourtail.minimap.client.gui.hud.overlay.mobicons;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class PlayerEntityRenderer extends AbstractEntityRenderer<Player> {
    //TODO

    @Override
    public boolean canUseFor(LivingEntity entity) {
        return false;
    }

    @Override
    public void render(PoseStack stack, LivingEntity entity, float maxSize) {
        //TODO
    }

}
