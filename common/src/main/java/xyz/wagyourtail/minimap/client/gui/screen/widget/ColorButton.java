package xyz.wagyourtail.minimap.client.gui.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;

import java.util.function.Consumer;

/**
 * unfinished
 */
public class ColorButton extends AbstractWidget {

    private final Consumer<Integer> colorSelected;

    public ColorButton(int i, int j, int k, int l, Consumer<Integer> colorSelected) {
        super(i, j, k, l, new TextComponent(""));
        this.colorSelected = colorSelected;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        tesselator.end();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.onPress(mouseX - this.x, mouseY - this.y);
    }

    public void onPress(double xCord, double yCord) {

    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {

    }

}
