package xyz.wagyourtail.minimap.api.client.config.overlay.mobicon;

import net.minecraft.world.entity.LivingEntity;

import java.util.function.Predicate;

public abstract class AbstractMobIconFilterSettings {

    public abstract Predicate<LivingEntity> compileFilter();

}
