package xyz.wagyourtail.minimap.client.gui.hud.overlay.mobicons;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.GlowSquid;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import org.jetbrains.annotations.Nullable;
import xyz.wagyourtail.minimap.client.gui.AbstractMapRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultEntityRenderer extends AbstractEntityRenderer<LivingEntity> {
    private static final EntityHeadOffset defaultOffset = new EntityHeadOffset(64, 32, 8, 8, 8, 8);
    private static final Map<Class<? extends LivingEntity>, EntityHeadOffset> texMap = new HashMap<>();
    private static List<Pair<Class<? extends LivingEntity>, EntityHeadOffset>> texOrdered = new ArrayList<>();

    static {
        register(Axolotl.class, 64, 64, 5, 6, 8, 5);
        register(PolarBear.class, 128, 64, 7, 7, 7, 7);
        register(Bat.class, 64, 64, 6, 6, 6, 6);
        register(Bee.class, 64, 64, 10, 10, 7, 7);
        register(Blaze.class, 64, 64, 8, 8, 8, 8);
        register(Cat.class, 64, 32, 5, 5, 5, 4);
        register(Chicken.class, 64, 32, 3, 3, 4, 5);
        register(Cow.class, 64, 32, 6, 6, 8, 8);
        register(Creeper.class, 64, 32, 8, 8, 8, 8);
        //TODO: custom renderer for dolphin
        //TODO: custom renderer for ender dragon
        register(EnderDragon.class, 256, 256, 128, 46, 16, 16);
        register(EnderMan.class, 64, 32, 8, 8, 8, 8);
        //TODO: custom renderer for endermite

        // fish
        register(Cod.class, 32, 32, 11, 3, 8, 4);
        register(Pufferfish.class, 32, 32, 8, 8, 8, 8);
        register(Salmon.class, 32, 32, 22, 3, 8, 4);
        //TODO: custom renderer for tropical fish (eyes different places on 2 skins)


        register(Fox.class, 48, 32, 11, 7, 8, 6);
        register(Ghast.class, 64, 32, 16, 16, 16, 16);
        //TODO: custom renderer for goat
        register(Guardian.class, 64, 64, 16, 16, 12, 12);
        register(Hoglin.class, 126, 64, 80, 1, 14, 19);
        //TODO: custom renderer for horse

        // illagers
        register(Evoker.class, 64, 64, 8, 8, 8, 10);
        register(Illusioner.class, 64, 64, 8, 8, 8, 10);
        register(Pillager.class, 64, 64, 8, 8, 8, 10);
        register(Ravager.class, 128, 128, 16, 16, 16, 20);
        register(Vex.class, 64, 64, 8, 8, 8, 8);
        register(Vindicator.class, 64, 64, 8, 8, 8, 10);
        register(AbstractIllager.class, 64, 64, 8, 8, 8, 10);

        register(IronGolem.class, 128, 128, 8, 8, 8, 10);
        //TODO: custom renderer for llama
        register(Panda.class, 64, 64, 9, 15, 12, 10);
        //TODO: custom renderer for parrot
        register(Phantom.class, 64, 64, 5, 5, 7, 3);
        register(Pig.class, 64, 32, 8, 8, 8, 8);

        // piglin
        register(Piglin.class, 64, 64, 8, 8, 8, 8);
        register(ZombifiedPiglin.class, 64, 64, 8, 8, 8, 8);
        register(PiglinBrute.class, 64, 64, 8, 8, 8, 8);
        register(AbstractPiglin.class, 64, 64, 8, 8, 8, 8);

        //TODO: custom renderer for rabbit
        register(Sheep.class, 64, 32, 7, 8, 8, 6);
        //TODO: custom renderer for shulker
        //TODO: custom renderer for silverfish
        register(Skeleton.class, 64, 32, 8, 8, 8, 8);
        register(SnowGolem.class, 64, 64, 8, 8, 8, 8);

        // slimes
        //TODO: custom renderer for magmaslime? maybe? or just move to show eyes?
        register(MagmaCube.class, 64, 32, 8, 8, 8, 8);
        register(Slime.class, 64, 32, 8, 8, 8, 8);

        // spiders
        register(CaveSpider.class, 64, 32, 40, 12, 8, 8);
        register(Spider.class, 64, 32, 40, 12, 8, 8);

        // squids
        register(GlowSquid.class, 64, 32, 12, 12, 12, 16);
        register(Squid.class, 64, 32, 12, 12, 12, 16);

        register(Strider.class, 64, 128, 16, 16, 16, 14);
        //TODO: custom renderer for turtle

        // villagers
        //TODO: custom renderer for villager to show profession
        register(Villager.class, 64, 64, 8, 8, 8, 10);
        register(WanderingTrader.class, 64, 64, 8, 8, 8, 10);
        register(AbstractVillager.class, 64, 64, 8, 8, 8, 10);


        register(Witch.class, 64, 128, 8, 8, 8, 10);
        //TODO: custom renderer for wither
        //TODO: custom renderer for wolf

        // zombies
        //TODO: custom renderer for drowned
        register(Drowned.class, 64, 64, 8, 8, 8, 8);
        register(Husk.class, 64, 64, 8, 8, 8, 8);
        //TODO: custom renderer for zombie villager to show profession
        register(ZombieVillager.class, 64, 64, 8, 8, 8, 10);
        register(Zombie.class, 64, 32, 8, 8, 8, 8);


        //register default
        register(LivingEntity.class, 64, 32, 8, 8, 8, 8);
    }

    public synchronized static EntityHeadOffset register(Class<? extends LivingEntity> entity, @Nullable EntityHeadOffset tex) {
        texOrdered.removeIf(e -> e.t == entity);
        if (tex == null) {
            return texMap.remove(entity);
        }
        texOrdered.add(new Pair<>(entity, tex));
        return texMap.put(entity, tex);
    }

    public synchronized static EntityHeadOffset register(Class<? extends LivingEntity> entity, int width, int height, float u, float v, float w, float h) {
        texOrdered.removeIf(e -> e.t == entity);
        EntityHeadOffset tex = new EntityHeadOffset(width, height, u, v, w, h);
        texOrdered.add(new Pair<>(entity, tex));
        return texMap.put(entity, tex);
    }

    public synchronized static EntityHeadOffset registerBefore(Class<? extends LivingEntity> before, Class<? extends LivingEntity> entity, int width, int height, float u, float v, float w, float h) {
        texOrdered.removeIf(e -> e.t == entity);
        EntityHeadOffset tex = new EntityHeadOffset(width, height, u, v, w, h);
        texOrdered.add(
            texOrdered.stream()
                .filter(e -> e.t == before).findFirst()
                .map(e -> texOrdered.indexOf(e)).orElse(texOrdered.size()),
            new Pair<>(entity, tex)
        );
        return texMap.put(entity, tex);
    }

    @Override
    public boolean canUseFor(LivingEntity entity) {
        return true;
    }

    @Override
    public void render(PoseStack stack, LivingEntity entity, float maxSize) {
        for (Pair<Class<? extends LivingEntity>, DefaultEntityRenderer.EntityHeadOffset> entry : texOrdered) {
            if (entry.t.isAssignableFrom(entity.getClass())) {
                // draw tex centered
                RenderSystem.setShaderTexture(0, minecraft.getEntityRenderDispatcher().getRenderer(entity).getTextureLocation(entity));
                entry.u.render(stack, maxSize);
                return;
            }
        }
    }

    public static record EntityHeadOffset(int width, int height, float u, float v, float w, float h) {
        public void render(PoseStack stack, float maxSize) {
            stack.translate(-maxSize / 2, -maxSize / 2, 1);
            if (h < w) {
                float diff = (w - h) / maxSize;
                AbstractMapRenderer.drawTex(stack, diff / 2, 0, maxSize - diff, maxSize, u / width, v / height, (u + w) / width, (v + h) / height);
            } else {
                float diff = (h - w) / maxSize;
                AbstractMapRenderer.drawTex(stack, 0, diff / 2, maxSize, maxSize - diff, u / width, v / height, (u + w) / width, (v + h) / height);
            }
        }
    }

    public static record Pair<T, U>(T t, U u) {};
}
