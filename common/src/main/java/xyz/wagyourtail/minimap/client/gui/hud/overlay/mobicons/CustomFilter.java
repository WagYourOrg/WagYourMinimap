package xyz.wagyourtail.minimap.client.gui.hud.overlay.mobicons;

import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import xyz.wagyourtail.config.field.BrigadierOptionsOverride;
import xyz.wagyourtail.config.field.Setting;
import xyz.wagyourtail.config.field.SettingsContainer;

import java.util.List;
import java.util.function.Predicate;

@SettingsContainer("gui.wagyourminimap.settings.mob_icon.filter.custom")
public class CustomFilter extends AbstractMobIconFilter {
    private final List<EntityType<?>> miscLiving = List.of(
        EntityType.VILLAGER,
        EntityType.IRON_GOLEM,
        EntityType.SNOW_GOLEM
    );
    @Setting(value = "gui.wagyourminimap.settings.mob_icon.filter.custom.mobs",
        options = "getMobOptions",
        setter = "setMobs")
    @BrigadierOptionsOverride(value = ResourceArgument.class, getter = {"getSummonableEntityType", "method_45610", "m_247713_"})
    public String[] mobs = new String[0];
    private Predicate<LivingEntity> compiled = compileFilter();

    public List<String> getMobOptions() {
        return BuiltInRegistries.ENTITY_TYPE.entrySet().stream().filter(e ->
            e.getValue().getCategory() != MobCategory.MISC ||
                miscLiving.contains(e.getValue())
        ).map(e -> e.getKey().location().toString()).toList();
    }

    public void setMobs(String[] mobs) {
        this.mobs = mobs;
        compiled = compileFilter();
    }


    public Predicate<LivingEntity> compileFilter() {
        List<String> mobs = List.of(this.mobs);
        return (e) -> mobs.contains(BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString());
    }

    @Override
    public boolean test(LivingEntity entity) {
        return compiled.test(entity);
    }

}
