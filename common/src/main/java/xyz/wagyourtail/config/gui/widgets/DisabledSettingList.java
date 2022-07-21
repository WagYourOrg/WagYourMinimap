package xyz.wagyourtail.config.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

public class DisabledSettingList<T> extends ObjectSelectionList<DisabledSettingList.DisabledSettingEntry<T>> {

    private final Component title = new TranslatableComponent("gui.wagyourconfig.available");


    public DisabledSettingList(Minecraft minecraft, int i, int j) {
        super(minecraft, i, j, 32, j - 55 + 4, 36);
        this.centerListVertically = false;
        this.setRenderHeader(true, 13);
    }

    @Override
    public int getRowWidth() {
        return this.width;
    }

    @Override
    protected void renderHeader(PoseStack poseStack, int x, int y, Tesselator tessellator) {
        Component component = (new TextComponent("")).append(this.title).withStyle(
            ChatFormatting.UNDERLINE,
            ChatFormatting.BOLD
        );
        this.minecraft.font.draw(
            poseStack,
            component,
            (float) (x + this.width / 2 - this.minecraft.font.width(component) / 2),
            (float) Math.min(this.y0 + 3, y),
            0xFFFFFF
        );
    }

    @Override
    protected int getScrollbarPosition() {
        return this.x1 - 6;
    }

    public interface EntryController<T> {
        void select(DisabledSettingList.DisabledSettingEntry<T> option);

    }

    public static class DisabledSettingEntry<T> extends ObjectSelectionList.Entry<DisabledSettingEntry<T>> {
        private static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation(
            "textures/gui/resource_packs.png");

        private final Minecraft minecraft;
        private final DisabledSettingList.EntryController<T> parentScreen;
        private final DisabledSettingList<T> parent;
        private final Component name;
        public final T option;

        public DisabledSettingEntry(Minecraft minecraft, DisabledSettingList.EntryController<T> parentScreen, DisabledSettingList<T> parent, T option, Component name) {
            this.minecraft = minecraft;
            this.parentScreen = parentScreen;
            this.parent = parent;
            this.option = option;
            this.name = name;
        }

        @Override
        public Component getNarration() {
            return new TranslatableComponent("narrator.select", name);
        }

        @Override
        public void render(PoseStack matrixStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
            RenderSystem.setShaderTexture(0, ICON_OVERLAY_LOCATION);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            int i = mouseX - left;
            if (i < 16 && isMouseOver) {
                GuiComponent.blit(matrixStack, left, top, 32.0F, 32.0F, 32, 32, 256, 256);
            } else {
                GuiComponent.blit(matrixStack, left, top, 32.0F, 0.0F, 32, 32, 256, 256);
            }


            minecraft.font.draw(matrixStack, minecraft.font.split(name, width - 36).get(0), left + 36, top, 0xFFFFFF);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            double d = mouseX - (double) this.parent.getRowLeft();
            double e = mouseY - (double) this.parent.getRowTop(this.parent.children().indexOf(this));
            if (d < 16.0D) {
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                this.parentScreen.select(this);
                return true;
            }
            return false;
        }

    }

}
