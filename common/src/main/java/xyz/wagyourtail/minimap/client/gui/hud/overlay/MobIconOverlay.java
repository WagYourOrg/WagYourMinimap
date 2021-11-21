package xyz.wagyourtail.minimap.client.gui.hud.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.api.client.config.MinimapClientConfig;
import xyz.wagyourtail.minimap.client.gui.hud.map.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.overlay.mobicons.AbstractEntityRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.overlay.mobicons.JSONEntityRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.overlay.mobicons.VanillaEntityRenderer;

import java.util.ArrayList;
import java.util.List;

public class MobIconOverlay extends AbstractMinimapOverlay {
    public static final List<AbstractEntityRenderer<?>> availableMobIconRenderers = new ArrayList<>(List.of(
        new JSONEntityRenderer(),
        new VanillaEntityRenderer()
    ));

    public MobIconOverlay(AbstractMinimapRenderer parent) {
        super(parent);
    }

    @Override
    public void renderOverlay(PoseStack stack, @NotNull Vec3 center, float maxLength, @NotNull Vec3 player_pos, float player_rot) {
        int chunkRadius = MinimapClientApi.getInstance().getConfig().get(MinimapClientConfig.class).chunkRadius;
        int chunkDiam = chunkRadius * 2 - 1;
        float chunkScale = maxLength / ((float) chunkDiam - 1);

        float iconSize = Math.min(10, maxLength / 15);

        assert minecraft.level != null;
        for (Entity e : minecraft.level.entitiesForRendering()) {
            if (e instanceof LivingEntity le) {
                stack.pushPose();
                Vec3 pointVec = new Vec3(e.getX(), e.getY(), e.getZ()).subtract(center);
                if (parent.rotate) {
                    pointVec = pointVec.yRot((float) Math.toRadians(
                        player_rot - 180));
                }
                if (parent.scaleBy != 1) {
                    pointVec = pointVec.multiply(parent.scaleBy, 1, parent.scaleBy);
                }
                float scale = parent.getScaleForVecToBorder(pointVec, chunkRadius, maxLength);
                if (scale < 1) {
                    stack.popPose();
                    continue;
                }
                stack.translate(
                    maxLength / 2 + pointVec.x * chunkScale / 16f,
                    maxLength / 2 + pointVec.z * chunkScale / 16f,
                    0
                );
                renderEntity(stack, le, iconSize);
                stack.popPose();
            }
        }
    }

    public void renderEntity(PoseStack stack, LivingEntity e, float maxIconSize) {
        for (AbstractEntityRenderer<?> renderer : availableMobIconRenderers) {
            if (renderEntityInner(stack, renderer, e, maxIconSize)) return;
        }
    }

    private boolean renderEntityInner(PoseStack stack, AbstractEntityRenderer<?> renderer, LivingEntity e, float maxIconSize) {
        if (renderer.canUseFor(e)) {
            renderer.render(stack, e, maxIconSize);
            return true;
        }
        return false;
    }

}
