package xyz.wagyourtail.minimap.client.gui.hud.map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import xyz.wagyourtail.minimap.client.gui.hud.overlay.AbstractMinimapOverlay;
import xyz.wagyourtail.minimap.client.gui.hud.overlay.CircleMapBorderOverlay;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCircleMapRenderer extends AbstractMinimapRenderer {

    protected AbstractCircleMapRenderer(boolean rotate) {
        super(rotate, 1, true);
        this.availableOverlays.add(CircleMapBorderOverlay.class);
    }

    @Override
    public List<AbstractMinimapOverlay> getDefaultOverlays() {
        List<AbstractMinimapOverlay> list = new ArrayList<>(List.of(
            new CircleMapBorderOverlay(this)
        ));
        list.addAll(super.getDefaultOverlays());
        return list;
    }

    @Override
    public void drawStencil(PoseStack stack, float maxLength) {
        circle(stack, maxLength / 2, maxLength / 2, maxLength / 2, 50);
    }

    @Override
    public float getScaleForVecToBorder(Vec3 in, int chunkRadius, float maxLength) {
        return ((chunkRadius - 1) * 16f) / (float) in.horizontalDistance();
    }

    public void circle(PoseStack matrixStack, float x, float y, float radius, int segments) {
        matrixStack.pushPose();
        matrixStack.translate(x, y, 0);
        Matrix4f matrix = matrixStack.last().pose();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        float dAngle = (float) (2 * Math.PI / segments);
        builder.vertex(matrix, 0, 0, 0).color(0f, 0f, 0f, 1f).endVertex();
        builder.vertex(matrix, radius, 0, 0).color(0f, 0f, 0f, 1f).endVertex();
        float currentAngle = -dAngle;
        for (int i = 1; i < segments; i++) {
            builder.vertex(matrix, (float) Math.cos(currentAngle) * radius, (float) Math.sin(currentAngle) * radius, 0)
                .color(0f, 0f, 0f, 1f)
                .endVertex();
            currentAngle -= dAngle;
        }
        builder.vertex(matrix, radius, 0, 0).color(0f, 0f, 0f, 1f).endVertex();
        builder.end();
        BufferUploader.end(builder);
        matrixStack.popPose();
    }

}
