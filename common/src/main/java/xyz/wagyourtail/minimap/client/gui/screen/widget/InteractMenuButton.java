package xyz.wagyourtail.minimap.client.gui.screen.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

public class InteractMenuButton extends AbstractButton {

    public static final int btnHeight = 10;
    public static int color = 0xFF5B6EE1;
    public static int hoverColor = 0xFF306082;
    protected final Consumer<InteractMenuButton> onPress;

    public InteractMenuButton(Component component, Consumer<InteractMenuButton> onPress) {
        super(0, 0, 100, btnHeight, component);
        this.onPress = onPress;
    }


    @Override
    public void onPress() {
        this.onPress.accept(this);
    }


    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        fill(
            poseStack,
            this.getX(),
            this.getY(),
            this.getX() + this.width,
            this.getY() + this.height,
            this.isHovered ? hoverColor : color
        );
        int j = this.active ? 16777215 : 10526880;
        drawCenteredString(
            poseStack,
            minecraft.font,
            this.getMessage(),
            this.getX() + this.width / 2,
            this.getY() + (this.height - 8) / 2,
            j | Mth.ceil(this.alpha * 255.0F) << 24
        );
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

}
