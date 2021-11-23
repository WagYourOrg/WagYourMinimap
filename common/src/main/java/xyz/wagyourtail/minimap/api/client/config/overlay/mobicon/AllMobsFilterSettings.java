package xyz.wagyourtail.minimap.api.client.config.overlay.mobicon;

import net.minecraft.world.entity.LivingEntity;
import xyz.wagyourtail.config.field.SettingsContainer;

import java.util.function.Predicate;

@SettingsContainer("gui.wagyourminimap.settings.mob_icon.filter.all")
public class AllMobsFilterSettings extends AbstractMobIconFilterSettings {
    @Override
    public Predicate<LivingEntity> compileFilter() {
        return (e) -> true;
    }

}
