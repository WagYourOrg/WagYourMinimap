package xyz.wagyourtail.config.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class NamedEditBox extends EditBox {
    private final Font font;

    public NamedEditBox(Font font, int i, int j, int k, int l, Component component) {
        super(font, i, j + 6, k, l - 6, component);
        this.font = font;
    }


    @Override
    public void renderWidget(GuiGraphics poseStack, int mouseX, int mouseY, float partialTicks) {
        poseStack.pose().pushPose();
        poseStack.pose().translate(this.getX(), this.getY() - 6, 0);
        poseStack.pose().scale(.6f, .6f, 1);
        poseStack.drawString(font, this.getMessage(), 0, 0, 0xFFFFFF);
        poseStack.pose().popPose();
        super.renderWidget(poseStack, mouseX, mouseY, partialTicks);
    }

}
