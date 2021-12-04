package xyz.wagyourtail.minimap.client.gui.hud.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.WagYourMinimap;
import xyz.wagyourtail.minimap.client.gui.hud.map.AbstractMinimapRenderer;

@SettingsContainer("gui.wagyourminimap.settings.overlay.map_border")
public class CircleMapBorderOverlay extends AbstractMinimapOverlay {
    private static final ResourceLocation border = new ResourceLocation(
        WagYourMinimap.MOD_ID,
        "textures/circle_border.png"
    );

    public CircleMapBorderOverlay(AbstractMinimapRenderer parent) {
        super(parent);
    }

    @Override
    public void renderOverlay(PoseStack stack, @NotNull Vec3 center, float maxLength, @NotNull Vec3 player_pos, float player_rot) {
        RenderSystem.setShaderTexture(0, border);
        stack.translate(maxLength / 2, maxLength / 2, 0);
        Matrix4f matrix = stack.last().pose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_TEX);
        int segments = 64;
        float dAngle = (float) (2 * Math.PI / segments);
        builder.vertex(matrix, 0, 0, 0).uv(0, 1).endVertex();
        builder.vertex(matrix, maxLength, 0, 0).uv(0, 0).endVertex();
        float current_angle = -dAngle;
        int u_loop_at = segments / 8;
        float dU = 1f / u_loop_at;
        float current_u = dU;
        for (int i = 1; i < segments; i++) {
            builder.vertex(
                matrix,
                maxLength * (float) Math.cos(current_angle),
                maxLength * (float) Math.sin(current_angle),
                0
            ).uv(current_u, 0).endVertex();
            current_angle -= dAngle;
            if (i % u_loop_at == 0) {
                dU = -dU;
            }
            current_u += dU;
        }
        builder.vertex(matrix, maxLength, 0, 0).uv(0, 0).endVertex();
        builder.end();
        BufferUploader.end(builder);
        RenderSystem.disableBlend();
    }

}
