package xyz.wagyourtail.minimap.client.gui.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import xyz.wagyourtail.minimap.WagYourMinimap;

import java.util.function.Consumer;

public class MenuButton extends AbstractButton {
    private static final ResourceLocation button_base = new ResourceLocation(
        WagYourMinimap.MOD_ID,
        "textures/gui/button_base.png"
    );
    private static final ResourceLocation button_base_active = new ResourceLocation(
        WagYourMinimap.MOD_ID,
        "textures/gui/button_base_active.png"
    );

    private final ResourceLocation tex;

    protected final Consumer<MenuButton> onPress;

    public MenuButton(Component component, ResourceLocation tex, Consumer<MenuButton> onPress) {
        super(0, 0, 30, 30, component);
        this.tex = tex;
        this.onPress = onPress;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, isHovered() ? button_base_active : button_base);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        blit(poseStack, x, y, width, height, 0, 0, 64, 64, 64, 64);
        RenderSystem.setShaderTexture(0, tex);
        blit(poseStack, x, y, width, height, 0, 0, 64, 64, 64, 64);
        if (this.isHovered) {
            this.renderToolTip(poseStack, mouseX, mouseY);
        }

    }

    @Override
    public void renderToolTip(PoseStack poseStack, int mouseX, int mouseY) {
        Font font = Minecraft.getInstance().font;
        fill(
            poseStack,
            mouseX + 8,
            mouseY - 2,
            mouseX + font.width(getMessage()) + 12,
            mouseY + font.lineHeight + 2,
            0x7F000000
        );
        font.draw(poseStack, getMessage(), mouseX + 10, mouseY, 0xFFFFFF);
    }

    @Override
    public void onPress() {
        this.onPress.accept(this);
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        //TODO: figure out what this does?
    }

}
