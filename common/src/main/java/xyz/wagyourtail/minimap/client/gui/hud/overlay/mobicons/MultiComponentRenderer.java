package xyz.wagyourtail.minimap.client.gui.hud.overlay.mobicons;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.GlowSquid;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
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
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class MultiComponentRenderer extends AbstractEntityRenderer<LivingEntity> {
    private static final Map<Class<? extends LivingEntity>, Parts<?>> texMap = new HashMap<>();
    private static final List<Pair<Class<? extends LivingEntity>, Parts<?>>> texOrdered = new ArrayList<>();

    static {
        register(Axolotl.class, 64, 64,
            part(0, 0,  8, 5,  5, 6,  8, 5)
        );
        register(PolarBear.class, 128, 64,
            part(0, 0,  7, 7,  7, 7,  7, 7)
        );
        register(Bat.class, 64, 64,
            part(0, 0,  6, 6,  6, 6,  6, 6)
        );
        register(Bee.class, 64, 64,
            part(0, 0,  7, 7,  10, 10,  7, 7)
        );
        register(Blaze.class, 64, 64,
            part(0, 0,  8, 8,  8, 8,  8, 8)
        );
        register(Cat.class, 64, 32,
            part(0, 0,  5, 4,  5, 5,  5, 4)
        );
        register(Chicken.class, 64, 32,
            part(0, 0,  4, 5,  3, 3,  4, 5)
        );
        register(Cow.class, 64, 32,
            part(0, 0,  8, 8,  6, 6,  8, 8)
        );
        register(Creeper.class, 64, 32,
            part(0, 0,  8, 8,  8, 8,  8, 8)
        );
        //TODO: custom renderer for dolphin
        //TODO: better renderer for ender dragon
        register(EnderDragon.class, 256, 256,
            part(0, 0,  16, 16,  128, 46,  16, 16)
        );
        register(EnderMan.class, 64, 32,
            part(0, 0,  8, 8,  8, 8,  8, 8)
        );
        //TODO: custom renderer for endermite


        // fish
        {
            register(Cod.class, 32, 32,
                part(0, 0, 8, 4, 11, 3, 8, 4)
            );
            register(Pufferfish.class, 32, 32,
                part(0, 0, 8, 8, 8, 8, 8, 8)
            );
            register(Salmon.class, 32, 32,
                part(0, 0, 8, 4, 22, 3, 8, 4)
            );
            register(TropicalFish.class,
                32, 32,
                // tropical_a
                part(0, 1.5f, 6, 3, 4, 6, 6, 3),
                // tropical_b
                part(0, 0, 6, 6, 4, 26, 6, 6)
            );
        }


        register(Fox.class, 48, 32,
            part(0, 0,  8, 6,  11, 7,  8, 6)
        );
        register(Ghast.class, 64, 32,
            part(0, 0,  16, 16,  16, 16,  16, 16)
        );
        //TODO: custom renderer for goat
        register(Guardian.class, 64, 64,
            part(0, 0,  12, 12,  16, 16,  12, 12)
        );
        register(Hoglin.class, 126, 64,
            part(0, 0,  14, 19,  80, 1,  14, 19)
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
            part(0, 0,  8, 10,  8, 8,  8, 10)
        );
        register(Panda.class, 64, 64,
            part(0, 0,  12, 10,  9, 15,  12, 10)
        );
        //TODO: custom renderer for parrot
        register(Phantom.class, 64, 64,
            part(0, 0,  7, 3,  5, 5,  7, 3)
        );
        register(Pig.class, 64, 32,
            part(0, 0,  8, 8,  8, 8,  8, 8)
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
            register(Player.class,
                new Parts<>(64, 64, 1,
                    part(0, 0, 8, 8, 8, 8, 8, 8)
                ) {
                    @Override
                    public void bindTex(Player entity) {
                        PlayerInfo info = minecraft.getConnection().getPlayerInfo(entity.getUUID());
                        RenderSystem.setShaderTexture(0, info.getSkinLocation());
                    }

                    @Override
                    public void render(PoseStack stack, Player entity, float maxSize) {
                        if (entity == minecraft.getCameraEntity()) return; // don't render the controlled entity, it's already the arrow
                        super.render(stack, entity, maxSize);
                    }
                }
            );

        }

        //TODO: custom renderer for rabbit
        register(Sheep.class, 64, 32,
            part(0, 0,  8, 6,  7, 8,  8, 6)
        );
        //TODO: custom renderer for shulker
        //TODO: custom renderer for silverfish


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
            part(0, 0,  8, 8,  8, 8,  8, 8)
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
            part(0, 0,  16, 14,  16, 16,  16, 14)
        );
        //TODO: custom renderer for turtle

        // villagers
        {
            //TODO: custom renderer for villager/show profession
            register(Villager.class, 64, 64,
                part(0, 0, 8, 10, 8, 8, 8, 10)
            );
            register(WanderingTrader.class, 64, 64,
                part(0, 0, 8, 10, 8, 8, 8, 10)
            );
            register(AbstractVillager.class, 64, 64,
                part(0, 0, 8, 10, 8, 8, 8, 10)
            );
        }


        register(Witch.class, 64, 128,
            part(0, 0,  8, 10,  8, 8,  8, 10)
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
                part(0, 0, 8, 10, 8, 8, 8, 10)
            );
            register(Zombie.class, 64, 32,
                part(0, 0, 8, 8, 8, 8, 8, 8)
            );
        }


        //register default
        register(LivingEntity.class, 64, 32,
            part(0, 0,  8, 8,  8, 8,  8, 8)
        );
    }

    public static <T extends LivingEntity> Parts<?> register(Class<T> entity, @Nullable Parts<T> parts) {
        texOrdered.removeIf(p -> p.t.equals(entity));
        if (parts == null) {
            return texMap.remove(entity);
        }
        texOrdered.add(new Pair<>(entity, parts));
        return texMap.put(entity, parts);
    }

    public static <T extends LivingEntity> Parts<?> registerBefore(Class<? extends LivingEntity> before, Class<T> entity, @Nullable Parts<T> parts) {
        texOrdered.removeIf(e -> e.t == entity);
        if (parts == null) return texMap.remove(entity);
        texOrdered.add(
            texOrdered.stream()
                .filter(e -> e.t == before).findFirst()
                .map(texOrdered::indexOf).orElse(texOrdered.size()),
            new Pair<>(entity, parts)
        );
        return texMap.put(entity, parts);
    }

    public static <T extends LivingEntity> Part<T> part(float x, float y, float w, float h, float u, float v, float uw, float vh) {
        return new Part<>(x, y, w, h, u, v, uw, vh);
    }

    @SafeVarargs
    public static <T extends LivingEntity> Parts<?> register(Class<T> entity, int texWidth, int texHeight, Part<T>... parts) {
        return register(entity, new Parts<>(texWidth, texHeight, 1f, parts));
    }

    @SafeVarargs
    public static <T extends LivingEntity> Parts<?> register(Class<T> entity, int texWidth, int texHeight, float scale, Part<T>... parts) {
        return register(entity, new Parts<>(texWidth, texHeight, scale, parts));
    }

    @SafeVarargs
    public static <T extends LivingEntity> Parts<?> registerBefore(Class<? extends LivingEntity> before, Class<T> entity, int texWidth, int texHeight, Part<T>... parts) {
        texOrdered.removeIf(e -> e.t == entity);
        Parts<T> tex = new Parts<>(texWidth, texHeight, 1f, parts);
        texOrdered.add(
            texOrdered.stream()
                .filter(e -> e.t == before).findFirst()
                .map(texOrdered::indexOf).orElse(texOrdered.size()),
            new Pair<>(entity, tex)
        );
        return texMap.put(entity, tex);
    }

    @SafeVarargs
    public static <T extends LivingEntity> Parts<?> registerBefore(Class<? extends LivingEntity> before, Class<T> entity, int width, int height, float scale, Part<T>... parts) {
        texOrdered.removeIf(e -> e.t == entity);
        Parts<T> tex = new Parts<>(width, height, scale, parts);
        texOrdered.add(
            texOrdered.stream()
                .filter(e -> e.t == before).findFirst()
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
    public  void render(PoseStack stack, LivingEntity entity, float maxSize) {
        for (Pair<Class<? extends LivingEntity>, Parts<?>> tex : texOrdered) {
            if (tex.t.isAssignableFrom(entity.getClass())) {
                maxSize = maxSize * tex.u.scale;
                stack.translate(-maxSize / 2, -maxSize / 2, 1);
                // cast to base, so we can compile
                ((Parts<LivingEntity>) tex.u).render(stack, entity, maxSize);
                return;
            }
        }

    }

    public static class Parts<T extends LivingEntity> {
        private final int texWidth;
        private final int texHeight;
        private final float scale;
        private final Part<T>[] parts;

        @SafeVarargs
        public Parts(int texWidth, int texHeight, float scale, Part<T>... parts) {
            this.texWidth = texWidth;
            this.texHeight = texHeight;
            this.scale = scale;
            this.parts = parts;
        }

        public void bindTex(T entity) {
            RenderSystem.setShaderTexture(0, minecraft.getEntityRenderDispatcher().getRenderer(entity).getTextureLocation(entity));
        }

        public void render(PoseStack stack, T entity, float maxSize) {
            float minX = 0;
            float maxX = 0;
            float minY = 0;
            float maxY = 0;
            for (Part<T> part : parts) {
                minX = Math.min(minX, part.x);
                maxX = Math.max(maxX, part.x + part.w);
                minY = Math.min(minY, part.y);
                maxY = Math.max(maxY, part.y + part.h);
            }
            float xW = maxX - minX;
            float yH = maxY - minY;
            assert minX == 0 && minY == 0 : "Parts must start at >= (0, 0)";
            renderInner(stack, entity, maxSize, minX, minY, maxX, maxY, xW, yH);
        }

        public void renderInner(PoseStack stack, T entity, float maxSize, float minX, float minY, float maxX, float maxY, float xW, float yH) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableTexture();Matrix4f matrix = stack.last().pose();
            BufferBuilder builder = Tesselator.getInstance().getBuilder();
            boolean prevTexed = true;
            if (xW < yH) {
                float diff = (yH - xW) / 2;
                float scale = maxSize / yH;
                for (Part<T> part : parts) {
                    if (part instanceof TexturedPart<T> tp) {
                        builder.end();
                        BufferUploader.end(builder);
                        tp.bindTex(entity);
                        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                        tp.render(matrix, builder, diff, 0, scale, tp.texWidth, tp.texHeight);
                        builder.end();
                        BufferUploader.end(builder);
                        prevTexed = true;
                    } else if (prevTexed) {
                        bindTex(entity);
                        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                        prevTexed = false;
                    }
                    part.render(matrix, builder, diff, 0, scale, texWidth, texHeight);
                }
            } else {
                float diff = (xW - yH) / 2;
                float scale = maxSize / xW;
                for (Part<T> part : parts) {
                    if (part instanceof TexturedPart<T> tp) {
                        builder.end();
                        BufferUploader.end(builder);
                        tp.bindTex(entity);
                        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                        tp.render(matrix, builder, 0, diff, scale, tp.texWidth, tp.texHeight);
                        builder.end();
                        BufferUploader.end(builder);
                        prevTexed = true;
                    } else if (prevTexed) {
                        bindTex(entity);
                        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                        prevTexed = false;
                    }
                    part.render(matrix, builder, 0, diff, scale, texWidth, texHeight);
                }
            }
            if (!prevTexed) {
                builder.end();
                BufferUploader.end(builder);
            }
        }
    }

    public static class Part<T extends LivingEntity> {
        public final float x;
        public final float y;
        public final float w;
        public final float h;
        public final float ux;
        public final float vy;
        public final float uw;
        public final float vh;

        public Part(float x, float y, float w, float h, float ux, float vy, float uw, float vh) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.ux = ux;
            this.vy = vy;
            this.uw = uw;
            this.vh = vh;
        }

        public void render(Matrix4f matrix, BufferBuilder builder, float xdiff, float ydiff, float scale, int texWidth, int texHeight) {
            drawTex(matrix, builder, (x + xdiff) * scale, (y + ydiff) * scale, w * scale, h * scale, ux / texWidth, vy / texHeight, (ux + uw) / texWidth, (vy + vh) / texHeight);
        }

        public static void drawTex(Matrix4f matrix, BufferBuilder builder, float x, float y, float width, float height, float startU, float startV, float endU, float endV) {
            builder.vertex(matrix, x, y + height, 0).uv(startU, endV).endVertex();
            builder.vertex(matrix, x + width, y + height, 0).uv(endU, endV).endVertex();
            builder.vertex(matrix, x + width, y, 0).uv(endU, startV).endVertex();
            builder.vertex(matrix, x, y, 0).uv(startU, startV).endVertex();
        }

    }

    public static abstract class TexturedPart<T extends LivingEntity> extends Part<T> {
        private final int texWidth;
        private final int texHeight;


        public TexturedPart(int texWidth, int texHeight, float x, float y, float w, float h, float ux, float vy, float uw, float vh) {
            super(x, y, w, h, ux, vy, uw, vh);
            this.texWidth = texWidth;
            this.texHeight = texHeight;
        }

        abstract void bindTex(T entity);

    }

    public static record Pair<T, U>(T t, U u) {}
}
