package xyz.wagyourtail.config.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;

import java.awt.*;
import java.util.function.Consumer;

public class ColorButton extends AbstractWidget {

    private final Consumer<Integer> colorSelected;
    private float h, s, v;

    public ColorButton(int i, int j, int k, int l, int currentColor, Consumer<Integer> colorSelected) {
        super(i, j, k, l, net.minecraft.network.chat.Component.literal(""));
        this.colorSelected = colorSelected;
        setCurrentColor(currentColor);
    }

    public void setCurrentColor(int currentColor) {
        float[] hsv = Color.RGBtoHSB(currentColor >> 16 & 255, currentColor >> 8 & 255, currentColor & 255, null);
        this.h = hsv[0];
        this.s = hsv[1];
        this.v = hsv[2];
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        // hue across the bottom, brightness top-bottom, sidebar on right for saturation
        float w = (width - 30) / 6f;
        bufferBuilder.vertex(getX(), getY(), 0).color(0f, 0f, 0f, 1f).endVertex();
        bufferBuilder.vertex(getX(), getY() + height, 0).color(0f, 0f, 0f, 1f).endVertex();
        bufferBuilder.vertex(getX() + w * 6, getY() + height, 0).color(0f, 0f, 0f, 1f).endVertex();
        bufferBuilder.vertex(getX() + w * 6, getY(), 0).color(0f, 0f, 0f, 1f).endVertex();


        bufferBuilder.vertex(getX(), getY(), 0).color(1f, 0f, 0f, 1f).endVertex();
        bufferBuilder.vertex(getX(), getY() + height, 0).color(1f, 0f, 0f, 0f).endVertex();
        bufferBuilder.vertex(getX() + w, getY() + height, 0).color(1f, 1f, 0f, 0f).endVertex();
        bufferBuilder.vertex(getX() + w, getY(), 0).color(1f, 1f, 0f, 1f).endVertex();

        bufferBuilder.vertex(getX() + w, getY(), 0).color(1f, 1f, 0f, 1f).endVertex();
        bufferBuilder.vertex(getX() + w, getY() + height, 0).color(1f, 1f, 0f, 0f).endVertex();
        bufferBuilder.vertex(getX() + w * 2, getY() + height, 0).color(0f, 1f, 0f, 0f).endVertex();
        bufferBuilder.vertex(getX() + w * 2, getY(), 0).color(0f, 1f, 0f, 1f).endVertex();

        bufferBuilder.vertex(getX() + w * 2, getY(), 0).color(0f, 1f, 0f, 1f).endVertex();
        bufferBuilder.vertex(getX() + w * 2, getY() + height, 0).color(0f, 1f, 0f, 0f).endVertex();
        bufferBuilder.vertex(getX() + w * 3, getY() + height, 0).color(0f, 1f, 1f, 0f).endVertex();
        bufferBuilder.vertex(getX() + w * 3, getY(), 0).color(0f, 1f, 1f, 1f).endVertex();

        bufferBuilder.vertex(getX() + w * 3, getY(), 0).color(0f, 1f, 1f, 1f).endVertex();
        bufferBuilder.vertex(getX() + w * 3, getY() + height, 0).color(0f, 1f, 1f, 0f).endVertex();
        bufferBuilder.vertex(getX() + w * 4, getY() + height, 0).color(0f, 0f, 1f, 0f).endVertex();
        bufferBuilder.vertex(getX() + w * 4, getY(), 0).color(0f, 0f, 1f, 1f).endVertex();

        bufferBuilder.vertex(getX() + w * 4, getY(), 0).color(0f, 0f, 1f, 1f).endVertex();
        bufferBuilder.vertex(getX() + w * 4, getY() + height, 0).color(0f, 0f, 1f, 0f).endVertex();
        bufferBuilder.vertex(getX() + w * 5, getY() + height, 0).color(1f, 0f, 1f, 0f).endVertex();
        bufferBuilder.vertex(getX() + w * 5, getY(), 0).color(1f, 0f, 1f, 1f).endVertex();

        bufferBuilder.vertex(getX() + w * 5, getY(), 0).color(1f, 0f, 1f, 1f).endVertex();
        bufferBuilder.vertex(getX() + w * 5, getY() + height, 0).color(1f, 0f, 1f, 0f).endVertex();
        bufferBuilder.vertex(getX() + w * 6, getY() + height, 0).color(1f, 0f, 0f, 0f).endVertex();
        bufferBuilder.vertex(getX() + w * 6, getY(), 0).color(1f, 0f, 0f, 1f).endVertex();

        int colFullSat = Color.HSBtoRGB(h, 1f, v);
        float colFullSatRed = (colFullSat >> 16 & 255) / 255f;
        float colFullSatGreen = (colFullSat >> 8 & 255) / 255f;
        float colFullSatBlue = (colFullSat & 255) / 255f;
        float colorNoSat = Math.max(Math.max(colFullSatRed, colFullSatGreen), colFullSatBlue);

        bufferBuilder.vertex(getX() + w * 6 + 10, getY(), 0)
            .color(colFullSatRed, colFullSatGreen, colFullSatBlue, 1f)
            .endVertex();
        bufferBuilder.vertex(getX() + w * 6 + 10, getY() + height, 0).color(colorNoSat, colorNoSat, colorNoSat, 1f).endVertex();
        bufferBuilder.vertex(getX() + w * 6 + 30, getY() + height, 0).color(colorNoSat, colorNoSat, colorNoSat, 1f).endVertex();
        bufferBuilder.vertex(getX() + w * 6 + 30, getY(), 0)
            .color(colFullSatRed, colFullSatGreen, colFullSatBlue, 1f)
            .endVertex();

        // draw location in saturation
        float h = height * (1f - s);
        bufferBuilder.vertex(getX() + w * 6 + 10, getY() + h, 0).color(0f, 0f, 0f, 1f).endVertex();
        bufferBuilder.vertex(getX() + w * 6 + 10, getY() + h + 1, 0).color(0f, 0f, 0f, 1f).endVertex();
        bufferBuilder.vertex(getX() + w * 6 + 30, getY() + h + 1, 0).color(0f, 0f, 0f, 1f).endVertex();
        bufferBuilder.vertex(getX() + w * 6 + 30, getY() + h, 0).color(0f, 0f, 0f, 1f).endVertex();

        // draw location in hb graph
        float locX = (w * 6) * this.h;
        float locY = height * (1 - v);
        bufferBuilder.vertex(getX() + locX - 2, getY() + locY - 2, 0).color(0f, 0f, 0f, 1f).endVertex();
        bufferBuilder.vertex(getX() + locX - 2, getY() + locY + 2, 0).color(0f, 0f, 0f, 1f).endVertex();
        bufferBuilder.vertex(getX() + locX + 2, getY() + locY + 2, 0).color(0f, 0f, 0f, 1f).endVertex();
        bufferBuilder.vertex(getX() + locX + 2, getY() + locY - 2, 0).color(0f, 0f, 0f, 1f).endVertex();

        int col = Color.HSBtoRGB(this.h, s, v);
        float colRed = (col >> 16 & 255) / 255f;
        float colGreen = (col >> 8 & 255) / 255f;
        float colBlue = (col & 255) / 255f;
        bufferBuilder.vertex(getX() + locX - 1, getY() + locY - 1, 0).color(colRed, colGreen, colBlue, 1f).endVertex();
        bufferBuilder.vertex(getX() + locX - 1, getY() + locY + 1, 0).color(colRed, colGreen, colBlue, 1f).endVertex();
        bufferBuilder.vertex(getX() + locX + 1, getY() + locY + 1, 0).color(colRed, colGreen, colBlue, 1f).endVertex();
        bufferBuilder.vertex(getX() + locX + 1, getY() + locY - 1, 0).color(colRed, colGreen, colBlue, 1f).endVertex();

        tesselator.end();
        RenderSystem.disableBlend();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX < getX() || mouseX > getX() + width || mouseY < getY() || mouseY > getY() + height) {
            return false;
        }
        if (button == 0) {
            adjustColor(mouseX, mouseY);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (mouseX < getX() || mouseX > getX() + width || mouseY < getY() || mouseY > getY() + height) {
            return false;
        }
        if (button == 0) {
            adjustColor(mouseX, mouseY);
            return true;
        }
        return false;
    }

    private void adjustColor(double mouseX, double mouseY) {
        mouseX -= getX();
        mouseY -= getY();
        mouseX = Mth.clamp(mouseX, 0, width);
        mouseY = Mth.clamp(mouseY, 0, height);
        if (mouseX <= width - 30) {
            h = (float) mouseX / (width - 30);
            s = 1;
            v = 1 - (float) mouseY / height;
            colorSelected.accept(Color.HSBtoRGB(h, s, v));
        } else if (mouseX >= width - 20) {
            s = 1f - (float) mouseY / height;
            colorSelected.accept(Color.HSBtoRGB(h, s, v));
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

}
