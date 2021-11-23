package xyz.wagyourtail.minimap.api.client.config.overlay.mobicon;

import net.minecraft.core.Registry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import xyz.wagyourtail.config.field.Setting;
import xyz.wagyourtail.config.field.SettingsContainer;

import java.util.List;
import java.util.function.Predicate;

@SettingsContainer("gui.wagyourminimap.settings.mob_icon.filter.custom")
public class CustomFilterSettings extends AbstractMobIconFilterSettings {
    private final List<EntityType<?>> miscLiving = List.of(
        EntityType.VILLAGER,
        EntityType.IRON_GOLEM,
        EntityType.SNOW_GOLEM
    );

    @Setting(value = "gui.wagyourminimap.settings.mob_icon.filter.custom.mobs", options = "getMobOptions")
    public String[] mobs = new String[0];


    public List<String> getMobOptions() {
        return Registry.ENTITY_TYPE.entrySet().stream().filter(e ->
            e.getValue().getCategory() != MobCategory.MISC ||
                miscLiving.contains(e.getValue())
        ).map(e -> e.getKey().location().toString()).toList();
    }

    @Override
    public Predicate<LivingEntity> compileFilter() {
        List<String> mobs = List.of(this.mobs);
        return (e) -> mobs.contains(Registry.ENTITY_TYPE.getKey(e.getType()).toString());
    }

}
