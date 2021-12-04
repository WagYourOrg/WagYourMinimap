package xyz.wagyourtail.minimap.client.gui.hud.overlay.mobicons;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Enemy;
import xyz.wagyourtail.config.field.SettingsContainer;

@SettingsContainer("gui.wagyourminimap.settings.mob_icon.filter.hostile_neutral")
public class HostileAndNeutral extends AbstractMobIconFilter {

    @Override
    public boolean test(LivingEntity entity) {
        return entity instanceof Enemy || entity instanceof NeutralMob;
    }

}
