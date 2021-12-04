package xyz.wagyourtail.minimap.client.gui.hud.overlay.mobicons;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import xyz.wagyourtail.config.field.SettingsContainer;

@SettingsContainer("gui.wagyourminimap.settings.mob_icon.filter.hostile")
public class HostileMobFilter extends AbstractMobIconFilter {

    @Override
    public boolean test(LivingEntity entity) {
        return entity instanceof Enemy;
    }

}
