package xyz.wagyourtail.minimap.client.gui.screen.map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import xyz.wagyourtail.config.field.SettingsContainer;

@SettingsContainer("gui.wagyourminimap.settings.overlay.scale")
public class ScaleOverlay extends AbstractFullscreenOverlay {
    public ScaleOverlay(ScreenMapRenderer parent) {
        super(parent);
    }

    @Override
    public void renderOverlay(PoseStack stack, int mouseX, int mouseY) {
        stack.pushPose();
        float endX = parent.topX + parent.xDiam;
        float width = endX - parent.topX;

        float max_width = width / 3;
        int chunks = (int) (max_width / 16);
        int blocks;
        if (chunks == 0) {
            blocks = (int) Math.floor(max_width);
        } else {
            blocks = chunks * 16;
        }
        float length = blocks / width * parent.width;

        stack.translate(20, parent.height - 30, 0);

        fill(stack, 0, 8, length, 10, 0xFFFFFFFF);
        fill(stack, 0, 8, 2, 0, 0xFFFFFFFF);
        fill(stack, length - 2, 8, length, 0, 0xFFFFFFFF);

        GuiComponent.drawCenteredString(
            stack,
            minecraft.font,
            String.format("%d blocks", blocks),
            (int) length / 2,
            -3,
            0xFFFFFFFF
        );

        stack.popPose();
    }


    private void fill(PoseStack stack, float minX, float minY, float maxX, float maxY, int color) {
        Matrix4f matrix = stack.last().pose();
        float i;
        if (minX < maxX) {
            i = minX;
            minX = maxX;
            maxX = i;
        }

        if (minY < maxY) {
            i = minY;
            minY = maxY;
            maxY = i;
        }

        float f = (float) (color >> 24 & 255) / 255.0F;
        float g = (float) (color >> 16 & 255) / 255.0F;
        float h = (float) (color >> 8 & 255) / 255.0F;
        float j = (float) (color & 255) / 255.0F;
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix, minX, maxY, 0.0F).color(g, h, j, f).endVertex();
        bufferBuilder.vertex(matrix, maxX, maxY, 0.0F).color(g, h, j, f).endVertex();
        bufferBuilder.vertex(matrix, maxX, minY, 0.0F).color(g, h, j, f).endVertex();
        bufferBuilder.vertex(matrix, minX, minY, 0.0F).color(g, h, j, f).endVertex();
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

}
