package xyz.wagyourtail.minimap.client.gui.hud.overlay.mobicons;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.GlowSquid;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class VanillaEntityRenderer extends AbstractEntityRenderer<LivingEntity> {
    private static final Map<Class<? extends LivingEntity>, Parts<?>> texMap = new HashMap<>();
    private static final List<Pair<Class<? extends LivingEntity>, Parts<?>>> texOrdered = new ArrayList<>();

    static {
        register(Axolotl.class, 64, 64, .7f,
            part(0, 0, 8, 5, 5, 6, 8, 5)
        );
        register(PolarBear.class, 128, 64,
            part(0, 0, 7, 7, 7, 7, 7, 7)
        );
        register(Bat.class, 64, 64, .5f,
            part(0, 0, 6, 6, 6, 6, 6, 6)
        );
        register(Bee.class, 64, 64,
            part(0, 0, 7, 7, 10, 10, 7, 7)
        );
        register(Blaze.class, 64, 64,
            part(0, 0, 8, 8, 8, 8, 8, 8)
        );

        //cat
        {
            register(Cat.class, 64, 32, .8f,
                part(0, 1, 5, 4, 5, 5, 5, 4),
                //ears
                part(.5f, 0, 1, 1, 2, 12, 1, 1),
                part(3.5f, 0, 1, 1, 8, 12, 1, 1),
                // nose
                part(1, 3, 3, 2, 2, 26, 3, 2)
            );
            register(Ocelot.class, 64, 32, .8f,
                part(0, 1, 5, 4, 5, 5, 5, 4),
                //ears
                part(.5f, 0, 1, 1, 2, 12, 1, 1),
                part(3.5f, 0, 1, 1, 8, 12, 1, 1),
                // nose
                part(1, 3, 3, 2, 2, 26, 3, 2)
            );
        }
        register(Chicken.class, 64, 32, .7f,
            part(0, 0, 4, 5, 3, 3, 4, 5)
        );
        register(Cow.class, 64, 32,
            part(0, 0, 8, 8, 6, 6, 8, 8)
        );
        register(Creeper.class, 64, 32,
            part(0, 0, 8, 8, 8, 8, 8, 8)
        );
        register(Dolphin.class, 64, 64,
            part(0, 0, 6, 7, 0, 6, 6, 7),
            // nose
            part(6, 5, 4, 2, 0, 17, 4, 2)
        );
        //TODO: better renderer for ender dragon
        register(EnderDragon.class, 256, 256,
            part(0, 0, 16, 16, 128, 46, 16, 16)
        );
        register(EnderMan.class, 64, 32,
            part(0, 0, 8, 8, 8, 8, 8, 8),
            part(0, 0, 8, 8, 8, 24, 8, 8)
        );
        register(Endermite.class, 64, 32, .6f,
            part(0, 0, 4, 3, 2, 2, 4, 3)
        );


        // fish
        {
            register(Cod.class, 32, 32, .5f,
                part(0, 0, 8, 4, 11, 3, 8, 4)
            );
            register(Pufferfish.class, 32, 32, .5f,
                part(0, 0, 8, 8, 8, 8, 8, 8)
            );
            register(Salmon.class, 32, 32, .5f,
                part(0, 0, 8, 4, 22, 3, 8, 4)
            );
            register(TropicalFish.class, 32, 32, .5f,
                // don't need custom renderer because the corresponding area on the other texture is empty
                // tropical_a
                part(0, 1.5f, 6, 3, 4, 6, 6, 3),
                // tropical_b
                part(0, 0, 6, 6, 4, 26, 6, 6)
            );
        }


        register(Fox.class, 48, 32,
            part(0, 0, 8, 6, 11, 7, 8, 6)
        );
        register(Ghast.class, 64, 32,
            part(0, 0, 16, 16, 16, 16, 16, 16)
        );
        register(Goat.class, 64, 64,
            part(0, 0, 10, 7, 34, 56, 10, 7)
        );
        register(Guardian.class, 64, 64,
            part(0, 0, 12, 12, 16, 16, 12, 12)
        );
        register(Hoglin.class, 126, 64,
            part(0, 0, 14, 19, 80, 1, 14, 19)
        );

        // horse/llama
        {
            register(Llama.class, 128, 64,
                part(0, 0, 8, 8, 6, 20, 8, 8),
                // nose
                part(2, 2, 4, 4, 9, 9, 4, 4)
            );
            // donkey/horse/mule (all use same skin indexes)
            register(
                AbstractHorse.class, 64, 64,
                part(0, 0, 7, 5, 0, 20, 7, 5),
                part(7, .25f, 4.5f, 4.5f, 0, 30, 5, 5)
            );
        }

        // illagers
        {
            register(Evoker.class, 64, 64,
                part(0, 0, 8, 10, 8, 8, 8, 10)
            );
            register(Illusioner.class, 64, 64,
                part(0, 0, 8, 10, 8, 8, 8, 10)
            );
            register(Pillager.class, 64, 64,
                part(0, 0, 8, 10, 8, 8, 8, 10)
            );
            register(Ravager.class, 128, 128,
                part(0, 0, 16, 20, 16, 16, 16, 20)
            );
            register(Vex.class, 64, 64,
                part(0, 0, 8, 8, 8, 8, 8, 8)
            );
            register(Vindicator.class, 64, 64,
                part(0, 0, 8, 10, 8, 8, 8, 10)
            );
            register(AbstractIllager.class, 64, 64,
                part(0, 0, 8, 10, 8, 8, 8, 10)
            );
        }


        register(IronGolem.class, 128, 128,
            part(0, 0, 8, 10, 8, 8, 8, 10)
        );
        register(Panda.class, 64, 64,
            part(0, 0, 12, 10, 9, 15, 12, 10)
        );
        register(Parrot.class, 32, 32, .7f,
            part(0, 1, 2, 2, 2, 4, 2, 2),
            part(0, 0, 4, 1, 10, 4, 4, 1),
            part(2, 1, 2, 2, 12, 8, 2, 2)

        );
        register(Phantom.class, 64, 64,
            part(0, 0, 7, 3, 5, 5, 7, 3)
        );
        register(Pig.class, 64, 32,
            part(0, 0, 8, 8, 8, 8, 8, 8)
        );

        // piglin
        {
            register(Piglin.class, 64, 64,
                part(0, 0, 8, 8, 8, 8, 8, 8)
            );
            register(ZombifiedPiglin.class, 64, 64,
                part(0, 0, 8, 8, 8, 8, 8, 8)
            );
            register(PiglinBrute.class, 64, 64,
                part(0, 0, 8, 8, 8, 8, 8, 8)
            );
            register(AbstractPiglin.class, 64, 64,
                part(0, 0, 8, 8, 8, 8, 8, 8)
            );
        }


        // player
        {
            register(
                Player.class,
                new Parts<>(64, 64, 1,
                    part(0, 0, 8, 8, 8, 8, 8, 8)
                ) {
                    @Override
                    public void bindTex(Player entity) {
                        PlayerInfo info = minecraft.getConnection().getPlayerInfo(entity.getUUID());
                        RenderSystem.setShaderTexture(0, info.getSkinLocation());
                    }

                    @Override
                    public void render(PoseStack stack, Player entity, float maxSize, double yDiff) {
                        if (entity == minecraft.getCameraEntity()) {
                            return; // don't render the controlled entity, it's already the arrow
                        }
                        super.render(stack, entity, maxSize, yDiff);
                        if (Math.abs(yDiff) >= 1) {
                            return; // don't render if the player is past fade distance
                        }
                        stack.translate(maxSize / 2, maxSize, 0);
                        stack.scale(.5f, .5f, 1);
                        minecraft.font.draw(
                            stack,
                            entity.getDisplayName(),
                            -minecraft.font.width(entity.getDisplayName()) / 2f,
                            10,
                            0xFFFFFF
                        );
                    }
                }
            );

        }

        register(Rabbit.class, 64, 32, .8f,
            part(0, 5, 5, 4, 37, 5, 5, 4),
            part(0, 0, 2, 5, 53, 1, 2, 5),
            part(3, 0, 2, 5, 59, 1, 2, 5)
        );
        register(Sheep.class, 64, 32,
            part(0, 0, 8, 6, 7, 8, 8, 6)
        );
        register(Shulker.class, 64, 64,
            part(0, 0, 16, 12, 0, 16, 16, 12),
            part(5, 8, 6, 4, 6, 60, 6, 4),
            part(0, 12, 16, 8, 0, 44, 16, 8)
        );
        register(Silverfish.class, 64, 32, .5f,
            part(0, 0, 3, 2, 2, 2, 3, 2)
        );


        // skeleton
        {
            register(Skeleton.class, 64, 32,
                part(0, 0, 8, 8, 8, 8, 8, 8)
            );
            register(WitherSkeleton.class, 64, 32,
                part(0, 0, 8, 8, 8, 8, 8, 8)
            );
        }


        register(SnowGolem.class, 64, 64,
            part(0, 0, 8, 8, 8, 8, 8, 8)
        );


        // slimes
        {
            //TODO: multi-part renderer for magmaslime? maybe? or just move to show eyes?
            register(MagmaCube.class, 64, 32,
                part(0, 0, 8, 8, 8, 8, 8, 8)
            );

            register(Slime.class, 64, 32,
                part(0, 0, 8, 8, 8, 8, 8, 8)
            );
        }


        // spiders
        {
            register(CaveSpider.class, 64, 32,
                part(0, 0, 8, 8, 40, 12, 8, 8)
            );
            register(Spider.class, 64, 32,
                part(0, 0, 8, 8, 40, 12, 8, 8)
            );
        }


        // squids
        {
            register(GlowSquid.class, 64, 32,
                part(0, 0, 12, 16, 12, 12, 12, 16)
            );
            register(Squid.class, 64, 32,
                part(0, 0, 12, 16, 12, 12, 12, 16)
            );
        }

        register(Strider.class, 64, 128,
            part(0, 0, 16, 14, 16, 16, 16, 14)
        );
        //TODO: custom renderer for turtle

        // villagers
        {
            register(Villager.class, 64, 64,
                part(0, 0, 8, 10, 8, 8, 8, 10),
                new TexturedPart<>(64, 64,
                    0, 0, 8, 10, 40, 8, 8, 10
                ) {
                    @Override
                    boolean bindTex(Villager entity) {
                        if (entity.getVillagerData().getProfession() == VillagerProfession.NONE) {
                            return false;
                        }
                        RenderSystem.setShaderTexture(
                            0,
                            getResourceLocation(Registry.VILLAGER_PROFESSION.getKey(entity.getVillagerData()
                                .getProfession()))
                        );
                        return true;
                    }

                    private ResourceLocation getResourceLocation(ResourceLocation resourceLocation) {
                        return new ResourceLocation(
                            resourceLocation.getNamespace(),
                            "textures/entity/villager/profession/" + resourceLocation.getPath() + ".png"
                        );
                    }
                }
            );
            register(WanderingTrader.class, 64, 64,
                part(0, 0, 8, 10, 8, 8, 8, 10)
            );
            register(AbstractVillager.class, 64, 64,
                part(0, 0, 8, 10, 8, 8, 8, 10)
            );
        }


        register(Witch.class, 64, 128,
            part(0, 0, 8, 10, 8, 8, 8, 10)
        );
        //TODO: custom renderer for wither

        register(Wolf.class, 64, 32,
            part(0, 0, 6, 6, 4, 4, 6, 6),
            // nose
            part(1.5f, 3, 3, 3, 4, 14, 3, 3)
        );

        // zombies
        {
            //TODO: custom renderer for drowned, show both layers
            register(Drowned.class, 64, 64,
                part(0, 0, 8, 8, 8, 8, 8, 8)
            );
            register(Husk.class, 64, 64,
                part(0, 0, 8, 8, 8, 8, 8, 8)
            );
            //TODO: custom renderer for zombie villager to show profession
            register(ZombieVillager.class, 64, 64,
                part(0, 0, 8, 10, 8, 8, 8, 10),
                new TexturedPart<>(64, 64,
                    0, 0, 8, 10, 40, 8, 8, 10
                ) {
                    @Override
                    boolean bindTex(ZombieVillager entity) {
                        if (entity.getVillagerData().getProfession() == VillagerProfession.NONE) {
                            return false;
                        }
                        RenderSystem.setShaderTexture(
                            0,
                            getResourceLocation(Registry.VILLAGER_PROFESSION.getKey(entity.getVillagerData()
                                .getProfession()))
                        );
                        return true;
                    }

                    private ResourceLocation getResourceLocation(ResourceLocation resourceLocation) {
                        return new ResourceLocation(
                            resourceLocation.getNamespace(),
                            "textures/entity/zombie_villager/profession/" + resourceLocation.getPath() + ".png"
                        );
                    }
                }
            );
            register(Zombie.class, 64, 64,
                part(0, 0, 8, 8, 8, 8, 8, 8)
            );
        }

        //register default
        register(LivingEntity.class, 64, 32,
            part(0, 0, 8, 8, 8, 8, 8, 8)
        );
    }

    public static <T extends LivingEntity> Parts<?> registerBefore(Class<? extends LivingEntity> before, Class<T> entity, @Nullable Parts<T> parts) {
        texOrdered.removeIf(e -> e.t() == entity);
        if (parts == null) {
            return texMap.remove(entity);
        }
        texOrdered.add(
            texOrdered.stream()
                .filter(e -> e.t() == before).findFirst()
                .map(texOrdered::indexOf).orElse(texOrdered.size()),
            new Pair<>(entity, parts)
        );
        return texMap.put(entity, parts);
    }

    @SafeVarargs
    public static <T extends LivingEntity> Parts<?> register(Class<T> entity, int texWidth, int texHeight, Part<T>... parts) {
        return register(entity, new Parts<>(texWidth, texHeight, 1f, parts));
    }

    public static <T extends LivingEntity> Parts<?> register(Class<T> entity, @Nullable Parts<T> parts) {
        texOrdered.removeIf(p -> p.t().equals(entity));
        if (parts == null) {
            return texMap.remove(entity);
        }
        texOrdered.add(new Pair<>(entity, parts));
        return texMap.put(entity, parts);
    }

    @SafeVarargs
    public static <T extends LivingEntity> Parts<?> register(Class<T> entity, int texWidth, int texHeight, float scale, Part<T>... parts) {
        return register(entity, new Parts<>(texWidth, texHeight, scale, parts));
    }

    @SafeVarargs
    public static <T extends LivingEntity> Parts<?> registerBefore(Class<? extends LivingEntity> before, Class<T> entity, int texWidth, int texHeight, Part<T>... parts) {
        texOrdered.removeIf(e -> e.t() == entity);
        Parts<T> tex = new Parts<>(texWidth, texHeight, 1f, parts);
        texOrdered.add(
            texOrdered.stream()
                .filter(e -> e.t() == before).findFirst()
                .map(texOrdered::indexOf).orElse(texOrdered.size()),
            new Pair<>(entity, tex)
        );
        return texMap.put(entity, tex);
    }

    @SafeVarargs
    public static <T extends LivingEntity> Parts<?> registerBefore(Class<? extends LivingEntity> before, Class<T> entity, int width, int height, float scale, Part<T>... parts) {
        texOrdered.removeIf(e -> e.t() == entity);
        Parts<T> tex = new Parts<>(width, height, scale, parts);
        texOrdered.add(
            texOrdered.stream()
                .filter(e -> e.t() == before).findFirst()
                .map(texOrdered::indexOf).orElse(texOrdered.size()),
            new Pair<>(entity, tex)
        );
        return texMap.put(entity, tex);
    }


    @Override
    public boolean canUseFor(LivingEntity entity) {
        // optimistic check for exact match
        if (texMap.containsKey(entity.getClass())) {
            return true;
        }
        // check for superclass, these may result in bad rendering
        for (Class<? extends LivingEntity> e : texMap.keySet()) {
            if (e.isAssignableFrom(entity.getClass())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void render(PoseStack stack, LivingEntity entity, float maxSize, double yDiff) {
        for (Pair<Class<? extends LivingEntity>, Parts<?>> tex : texOrdered) {
            if (tex.t().isAssignableFrom(entity.getClass())) {
                // cast to base, so we can compile
                ((Parts<LivingEntity>) tex.u()).render(stack, entity, maxSize, yDiff);
                return;
            }
        }

    }

}
