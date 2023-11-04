package xyz.wagyourtail.minimap.client.gui.hud.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import xyz.wagyourtail.config.field.IntRange;
import xyz.wagyourtail.config.field.Setting;
import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.WagYourMinimap;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.api.client.config.MinimapClientConfig;
import xyz.wagyourtail.minimap.client.gui.AbstractMapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.map.AbstractMinimapRenderer;

@SettingsContainer("gui.wagyourminimap.settings.player_arrow")
public class PlayerArrowOverlay extends AbstractMinimapOverlay {
    private static final ResourceLocation player_icon_tex = new ResourceLocation(
        WagYourMinimap.MOD_ID,
        "textures/player_arrow.png"
    );

    @Setting(value = "gui.wagyourminimap.red")
    @IntRange(from = 0, to = 255)
    public int red = 0xFF;
    @Setting(value = "gui.wagyourminimap.green")
    @IntRange(from = 0, to = 255)
    public int green = 0x00;
    @Setting(value = "gui.wagyourminimap.blue")
    @IntRange(from = 0, to = 255)
    public int blue = 0x00;

    public PlayerArrowOverlay(AbstractMinimapRenderer parent) {
        super(parent);
    }

    @Override
    public void renderOverlay(GuiGraphics stack, @NotNull Vec3 center, float maxLength, @NotNull Vec3 player_pos, float player_rot) {
        int chunkRadius = MinimapClientApi.getInstance().getConfig().get(MinimapClientConfig.class).chunkRadius;

        int chunkDiam = chunkRadius * 2 - 1;
        float chunkScale = maxLength / ((float) chunkDiam - 1);

        Vec3 offset = center.subtract(player_pos);
        if (parent.rotate) {
            stack.pose().translate(maxLength / 2, maxLength / 2, 0);
            stack.pose().mulPose(new Quaternionf().rotateZ((float) Math.toRadians(player_rot - 180)));
            stack.pose().translate(-maxLength / 2, -maxLength / 2, 0);
        }

        stack.pose().translate(maxLength / 2 + offset.x * chunkScale / 16f, maxLength / 2 + offset.z * chunkScale / 16f, 0);
        stack.pose().mulPose(new Quaternionf().rotateZ((float) Math.toRadians(player_rot)));
        RenderSystem.setShaderTexture(0, player_icon_tex);
        float texSize = Math.max(maxLength / 20, 8);
        int color =  (blue << 16) | (green << 8) | red;
        AbstractMapRenderer.drawTexCol(stack, -texSize, -texSize, texSize * 2, texSize * 2, 1, 1, 0, 0, 0xFF000000 | color);
    }

}
