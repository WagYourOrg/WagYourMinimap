package xyz.wagyourtail.minimap.client.gui.hud.overlay.mobicons;

import net.minecraft.world.entity.LivingEntity;
import xyz.wagyourtail.config.field.SettingsContainer;

@SettingsContainer("gui.wagyourminimap.settings.mob_icon.filter.all")
public class AllMobsFilter extends AbstractMobIconFilter {

    @Override
    public boolean test(LivingEntity entity) {
        return true;
    }

}
