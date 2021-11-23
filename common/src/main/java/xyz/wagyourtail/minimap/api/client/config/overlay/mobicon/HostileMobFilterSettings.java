package xyz.wagyourtail.minimap.api.client.config.overlay.mobicon;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import xyz.wagyourtail.config.field.SettingsContainer;

import java.util.function.Predicate;

@SettingsContainer("gui.wagyourminimap.settings.mob_icon.filter.hostile")
public class HostileMobFilterSettings extends AbstractMobIconFilterSettings{
    @Override
    public Predicate<LivingEntity> compileFilter() {
        return (e) -> e instanceof Enemy;
    }

}
