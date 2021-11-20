package xyz.wagyourtail.minimap.client.gui.hud.overlay.mobicons;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.jetbrains.annotations.Nullable;
import xyz.wagyourtail.minimap.client.gui.AbstractMapRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiComponentRenderer extends AbstractEntityRenderer<LivingEntity> {
    private static final Map<Class<? extends LivingEntity>, Parts> texMap = new HashMap<>();
    private static List<DefaultEntityRenderer.Pair<Class<? extends LivingEntity>, Parts>> texOrdered = new ArrayList<>();

    static {
        //TODO: make everything use the multipart renderer instead of {@link DefaultEntityRenderer}
        register(
            AbstractHorse.class, 64, 64,
            part(0 , 0, 7, 5, 0, 20, 7, 5),
            part(7, .25f, 4.5f, 4.5f, 0, 30, 5, 5)
        );
    }

    public static Parts register(Class<? extends LivingEntity> entity, @Nullable Parts parts) {
        texOrdered.removeIf(p -> p.t().equals(entity));
        if (parts == null) {
            return texMap.remove(entity);
        }
        texOrdered.add(new DefaultEntityRenderer.Pair<>(entity, parts));
        return texMap.put(entity, parts);
    }

    public static Part part(float x, float y, float w, float h, float u, float v, float uw, float vh) {
        return new Part(x, y, w, h, u, v, uw, vh);
    }

    public static Parts register(Class<? extends LivingEntity> entity, int texWidth, int texHeight, Part... parts) {
        return register(entity, new Parts(texWidth, texHeight, 1f, parts));
    }

    public static Parts register(Class<? extends LivingEntity> entity, int texWidth, int texHeight, float scale, Part... parts) {
        return register(entity, new Parts(texWidth, texHeight, scale, parts));
    }

    public static Parts registerBefore(Class<? extends LivingEntity> before, Class<? extends LivingEntity> entity, int texWidth, int texHeight, Part... parts) {
        texOrdered.removeIf(e -> e.t() == entity);
        Parts tex = new Parts(texWidth, texHeight, 1f, parts);
        texOrdered.add(
            texOrdered.stream()
                .filter(e -> e.t() == before).findFirst()
                .map(e -> texOrdered.indexOf(e)).orElse(texOrdered.size()),
            new DefaultEntityRenderer.Pair<>(entity, tex)
        );
        return texMap.put(entity, tex);
    }

    public static Parts registerBefore(Class<? extends LivingEntity> before, Class<? extends LivingEntity> entity, int width, int height, float scale, Part... parts) {
        texOrdered.removeIf(e -> e.t() == entity);
        Parts tex = new Parts(width, height, scale, parts);
        texOrdered.add(
            texOrdered.stream()
                .filter(e -> e.t() == before).findFirst()
                .map(e -> texOrdered.indexOf(e)).orElse(texOrdered.size()),
            new DefaultEntityRenderer.Pair<>(entity, tex)
        );
        return texMap.put(entity, tex);
    }


    @Override
    public boolean canUseFor(LivingEntity entity) {
        for (Class<? extends LivingEntity> e : texMap.keySet()) {
            if (e.isAssignableFrom(entity.getClass())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void render(PoseStack stack, LivingEntity entity, float maxSize) {
        for (DefaultEntityRenderer.Pair<Class<? extends LivingEntity>, Parts> tex : texOrdered) {
            if (tex.t().isAssignableFrom(entity.getClass())) {
                maxSize = maxSize * tex.u().scale;
                stack.translate(-maxSize / 2, -maxSize / 2, 1);
                RenderSystem.setShaderTexture(0, minecraft.getEntityRenderDispatcher().getRenderer(entity).getTextureLocation(entity));
                tex.u().render(stack, maxSize);
                return;
            }
        }

    }

    public static record Parts(int texWidth, int texHeight, float scale, Part[] parts) {

        public void render(PoseStack stack, float maxSize) {
            float minX = 0;
            float maxX = 0;
            float minY = 0;
            float maxY = 0;
            for (Part part : parts) {
                minX = Math.min(minX, part.x);
                maxX = Math.max(maxX, part.x + part.w);
                minY = Math.min(minY, part.y);
                maxY = Math.max(maxY, part.y + part.h);
            }
            float xW = maxX - minX;
            float yH = maxY - minY;
            if (xW < yH) {
                float diff = (yH - xW);
                for (Part part : parts) {
                    AbstractMapRenderer.drawTex(stack, (part.x + diff / 2) * maxSize / yH, part.y * maxSize / yH, part.w * maxSize / yH, part.h * maxSize / yH, part.ux / texWidth, part.vy / texHeight, (part.ux + part.uw) / texWidth, (part.vy + part.vh) / texHeight);
                }
            } else {
                float diff = (xW - yH);
                for (Part part : parts) {
                    AbstractMapRenderer.drawTex(stack, part.x * maxSize / xW, (part.y + diff / 2) * maxSize / xW, part.w * maxSize / xW, part.h * maxSize / xW, part.ux / texWidth, part.vy / texHeight, (part.ux + part.uw) / texWidth, (part.vy + part.vh) / texHeight);
                }
            }
        }
    }

    public static record Part(float x, float y, float w, float h, float ux, float vy, float uw, float vh) {};
}
