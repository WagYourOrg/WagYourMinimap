package xyz.wagyourtail.minimap.client.gui.hud.map;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.api.client.config.MinimapClientConfig;
import xyz.wagyourtail.minimap.client.gui.AbstractMapRenderer;

public abstract class AbstractMinimapRenderer extends AbstractMapRenderer {
    public AbstractMapOverlayRenderer[] overlays = new AbstractMapOverlayRenderer[0];

    public void setOverlays(AbstractMapOverlayRenderer... overlays) {
        this.overlays = overlays;
    }

    public void render(PoseStack matrixStack, float tickDelta) {
        if (minecraft.options.renderDebug) {
            return;
        }
        matrixStack.pushPose();
        int w = minecraft.getWindow().getGuiScaledWidth();
        int h = minecraft.getWindow().getGuiScaledHeight();

        float minimapSize = Math.min(w, h) *
            MinimapClientApi.getInstance().getConfig().get(MinimapClientConfig.class).minimapScale / 100f;

        LocalPlayer player = minecraft.player;
        assert player != null;

        boolean bottom = MinimapClientApi.getInstance().getConfig().get(MinimapClientConfig.class).snapSide.bottom;

        float posX = MinimapClientApi.getInstance().getConfig().get(MinimapClientConfig.class).snapSide.right ?
            w - minimapSize - 10 :
            MinimapClientApi.getInstance().getConfig().get(MinimapClientConfig.class).snapSide.center ?
                w / 2f - minimapSize / 2f :
                10;
        float posZ = bottom ? h - minimapSize - minecraft.font.lineHeight - 10 : 10;
        Vec3 player_pos = player.getPosition(tickDelta);
        float player_rot = player.getYRot();

        //pull back map to 0, 0
        matrixStack.translate(posX, posZ, 0);
        matrixStack.pushPose();
        renderMinimap(matrixStack, player_pos, minimapSize, player_pos, player_rot);
        matrixStack.popPose();

        //DRAW OVERLAYS
        for (AbstractMapOverlayRenderer overlay : overlays) {
            matrixStack.pushPose();
            overlay.renderOverlay(matrixStack, player_pos, minimapSize, player_pos, player_rot);
            matrixStack.popPose();
        }
        matrixStack.popPose();

        //pull back text pos to 0, 0
        matrixStack.pushPose();
        matrixStack.translate(posX, posZ, 0);
        if (!bottom) {
            matrixStack.translate(0, minimapSize + 5, 0);
        }
        renderText(
            matrixStack,
            minimapSize,
            bottom,
            new TextComponent(String.format("%.2f %.2f %.2f", player_pos.x, player_pos.y, player_pos.z))
        );
        matrixStack.popPose();
    }

    public abstract void renderMinimap(PoseStack matrixStack, @NotNull Vec3 center, float maxLength, @NotNull Vec3 player_pos, float player_rot);

    public abstract void renderText(PoseStack matrixStack, float maxLength, boolean bottom, Component... textLines);

}
