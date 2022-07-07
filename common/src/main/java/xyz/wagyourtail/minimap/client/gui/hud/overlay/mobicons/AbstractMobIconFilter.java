package xyz.wagyourtail.minimap.client.gui.hud.overlay.mobicons;

import net.minecraft.world.entity.LivingEntity;

public abstract class AbstractMobIconFilter {
    public abstract boolean test(LivingEntity entity);

}
