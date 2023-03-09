package xyz.wagyourtail.minimap.client.gui.hud.overlay.mobicons;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import xyz.wagyourtail.minimap.api.MinimapApi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class JSONEntityRenderer extends AbstractEntityRenderer<LivingEntity> {
    private static final Map<EntityType<? extends LivingEntity>, Parts<?>> texMap = new HashMap<>();

    static {
        try {
            Path iconFile = MinimapApi.getInstance().configFolder.resolve("mobicons.json");
            if (!Files.exists(iconFile)) {
                Files.writeString(iconFile, "{}");
            }
            new JsonParser().parse(Files.readString(iconFile)).getAsJsonObject().entrySet().forEach(e -> {
                BuiltInRegistries.ENTITY_TYPE.getOptional(new ResourceLocation(e.getKey()))
                    .ifPresent(type -> {
                        texMap.put(
                            (EntityType<? extends LivingEntity>) type,
                            new Gson().fromJson(e.getValue(), Parts.class)
                        );
                    });
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean canUseFor(LivingEntity entity) {
        return texMap.containsKey(entity.getType());
    }

    @Override
    public void render(PoseStack stack, LivingEntity entity, float maxSize, double yDiff) {
        for (Map.Entry<EntityType<? extends LivingEntity>, Parts<?>> tex : texMap.entrySet()) {
            if (tex.getKey() == entity.getType()) {
                // cast to base, so we can compile
                ((Parts<LivingEntity>) tex.getValue()).render(stack, entity, maxSize, yDiff);
                return;
            }
        }
    }

}
