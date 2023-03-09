package xyz.wagyourtail.config.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class CombinedColorSelector extends AbstractWidget {

    public final ColorButton colorWheel;
    public final EditBox colorEdit;

    public final Font font;

    public Consumer<Integer> onColorChange;

    public CombinedColorSelector(int i, int j, int k, int l, int defCol, Font f, Component component) {
        super(i, j, k, l, component);
        int r = (defCol >> 16) & 255;
        int g = (defCol >> 8) & 255;
        int b = defCol & 255;
        this.font = f;

        colorWheel = new ColorButton(i, j + 14, k, l - 14, defCol, this::onColorWheel);
        colorEdit = new EditBox(f, i + l / 2, j, k, l / 2, component);
        colorEdit.setValue(
            zeroPad(Integer.toHexString(r & 255)) + zeroPad(Integer.toHexString(g & 255)) +
                zeroPad(Integer.toHexString(b & 255)));
        colorEdit.setFilter((s) -> s.matches("[\\da-fA-F]{0,6}"));
        colorEdit.setResponder(this::onColorMessage);
    }

    public void onColorMessage(String color) {
        int col = color.isEmpty() ? 0 : Integer.parseInt(color, 16);
        colorWheel.setCurrentColor(col);
        if (onColorChange != null) onColorChange.accept(col);
    }

    public void onColorWheel(int color) {
        int r = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int b = color & 255;

        colorEdit.setResponder(null);
        colorEdit.setValue(zeroPad(Integer.toHexString(r)) + zeroPad(Integer.toHexString(g)) +
            zeroPad(Integer.toHexString(b)));
        colorEdit.setResponder(this::onColorMessage);
        if (onColorChange != null) onColorChange.accept(color);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return colorWheel.mouseClicked(mouseX, mouseY, button) || colorEdit.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return colorEdit.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return colorEdit.charTyped(codePoint, modifiers);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        drawString(poseStack, font, getMessage(), getX(), getY(), 0xFFFFFF);
        colorWheel.render(poseStack, mouseX, mouseY, partialTick);
        colorEdit.render(poseStack, mouseX, mouseY, partialTick);
    }

    public String zeroPad(String in) {
        if (in.length() == 1) {
            return "0" + in;
        }
        return in;
    }
}
