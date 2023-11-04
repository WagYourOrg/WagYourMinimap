package xyz.wagyourtail.minimap.client.gui.hud.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import xyz.wagyourtail.config.field.DoubleRange;
import xyz.wagyourtail.config.field.IntRange;
import xyz.wagyourtail.config.field.Setting;
import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.api.client.config.MinimapClientConfig;
import xyz.wagyourtail.minimap.client.gui.hud.map.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.overlay.mobicons.*;

import java.util.*;

@SettingsContainer("gui.wagyourminimap.settings.overlay.mob_icon")
public class MobIconOverlay extends AbstractMinimapOverlay {
    public static final List<AbstractEntityRenderer<?>> availableMobIconRenderers = new ArrayList<>(List.of(
        new JSONEntityRenderer(),
        new VanillaEntityRenderer()
    ));

    public static final Set<Class<? extends AbstractMobIconFilter>> mobFilters = new HashSet<>(Set.of(
        HostileMobFilter.class,
        HostileAndNeutral.class,
        AllMobsFilter.class,
        CustomFilter.class
    ));

    @Setting(value = "gui.wagyourminimap.settings.overlay.mob_icon.max_scale")
    @DoubleRange(from = 0.0, to = 1.0, steps = 100)
    public double maxScale = .07;

    @Setting(value = "gui.wagyourminimap.settings.overlay.mob_icon.max_size")
    @IntRange(from = 0, to = 100)
    public int maxSize = 10;

    @Setting(value = "gui.wagyourminimap.settings.overlay.mob_icon.filter", options = "getMobFilters")
    public AbstractMobIconFilter filter = new HostileMobFilter();

    @Setting(value = "gui.wagyourminimap.settings.overlay.mob_icon.show_players")
    public boolean showPlayers = true;

    @Setting(value = "gui.wagyourminimap.settings.overlay.mob_icon.y_fade_distance")
    @IntRange(from = 0, to = 4096, stepVal = 8)
    public int yFadeDistance = 50;

    public MobIconOverlay(AbstractMinimapRenderer parent) {
        super(parent);
    }

    public void setFilterSettings(AbstractMobIconFilter settings) {

    }

    @Override
    public void renderOverlay(GuiGraphics stack, @NotNull Vec3 center, float maxLength, @NotNull Vec3 player_pos, float player_rot) {
        int chunkRadius = MinimapClientApi.getInstance().getConfig().get(MinimapClientConfig.class).chunkRadius;
        int chunkDiam = chunkRadius * 2 - 1;
        float chunkScale = maxLength / ((float) chunkDiam - 1);

        float iconSize = (float) Math.min(maxScale * maxLength, maxSize);

        assert minecraft.level != null;
        for (Entity e : minecraft.level.entitiesForRendering()) {
            if (e instanceof LivingEntity le && filter.test(le)) {
                //TODO: filtering settings
                stack.pose().pushPose();
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
                    stack.pose().popPose();
                    continue;
                }
                stack.pose().translate(
                    maxLength / 2 + pointVec.x * chunkScale / 16f,
                    maxLength / 2 + pointVec.z * chunkScale / 16f,
                    0
                );
                renderEntity(stack, le, iconSize, pointVec.y / yFadeDistance);
                stack.pose().popPose();
            }
        }
    }

    public void renderEntity(GuiGraphics stack, LivingEntity e, float maxIconSize, double yDiff) {
        for (AbstractEntityRenderer<?> renderer : availableMobIconRenderers) {
            if (renderEntityInner(stack, renderer, e, maxIconSize, yDiff)) {
                return;
            }
        }
    }

    private boolean renderEntityInner(GuiGraphics stack, AbstractEntityRenderer<?> renderer, LivingEntity e, float maxIconSize, double yDiff) {
        if (renderer.canUseFor(e)) {
            renderer.render(stack, e, maxIconSize, yDiff);
            return true;
        }
        return false;
    }

    public Collection<Class<? extends AbstractMobIconFilter>> getMobFilters() {
        return mobFilters;
    }

}
