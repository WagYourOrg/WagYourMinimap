package xyz.wagyourtail.minimap.client.gui.hud.overlay.mobicons;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;

public abstract class AbstractEntityRenderer<T extends LivingEntity> {
    protected static final Minecraft minecraft = Minecraft.getInstance();
    public abstract boolean canUseFor(LivingEntity entity);

    public abstract void render(PoseStack stack, LivingEntity entity, float maxSize);
}
