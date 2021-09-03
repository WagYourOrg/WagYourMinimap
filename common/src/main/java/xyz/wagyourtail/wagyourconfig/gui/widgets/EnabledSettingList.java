package xyz.wagyourtail.wagyourconfig.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import xyz.wagyourtail.wagyourconfig.field.SettingsContainer;
import xyz.wagyourtail.wagyourconfig.gui.SettingScreen;

public class EnabledSettingList<T> extends ObjectSelectionList<EnabledSettingList.EnabledSettingEntry<T>> {

    private final Component title = new TranslatableComponent("gui.wagyourconfig.enabled");

    public EnabledSettingList(Minecraft minecraft, int i, int j) {
        super(minecraft, i, j, 32, j - 55 + 4, 36);
        this.centerListVertically = false;
        this.setRenderHeader(true, 13);
    }

    @Override
    protected void renderHeader(PoseStack poseStack, int x, int y, Tesselator tessellator) {
        Component component = (new TextComponent("")).append(this.title).withStyle(ChatFormatting.UNDERLINE, ChatFormatting.BOLD);
        this.minecraft.font.draw(poseStack, component, (float)(x + this.width / 2 - this.minecraft.font.width(component) / 2), (float)Math.min(this.y0 + 3, y), 0xFFFFFF);
    }

    @Override
    public int getRowWidth() {
        return this.width;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.x1 - 6;
    }

    public interface EntryController<T> {
        boolean canMoveUp(EnabledSettingList.EnabledSettingEntry<T> option);
        boolean canMoveDown(EnabledSettingList.EnabledSettingEntry<T> option);
        void moveUp(EnabledSettingList.EnabledSettingEntry<T> option);
        void moveDown(EnabledSettingList.EnabledSettingEntry<T> option);
        void unselect(EnabledSettingList.EnabledSettingEntry<T> option);
    }

    public static class EnabledSettingEntry<T> extends ObjectSelectionList.Entry<EnabledSettingEntry<T>> {
        private static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/resource_packs.png");
        private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
        private final Minecraft minecraft;
        private final EntryController<T> parentScreen;
        private final EnabledSettingList<T> parent;
        private final Component name;
        public final T option;

        public EnabledSettingEntry(Minecraft minecraft, EntryController<T> parentScreen, EnabledSettingList<T> parent, T option, Component name) {
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
            int j = mouseY - top;

            if (i < 32 && isMouseOver) {
                GuiComponent.blit(matrixStack, left - 8, top, 0.0F, 32.0F, 32, 32, 256, 256);
            } else {
                GuiComponent.blit(matrixStack, left - 8, top, 0.0F, 0.0F, 32, 32, 256, 256);
            }

            if (parentScreen.canMoveUp(this)) {
                if (i < 32 && i > 16 && j < 16 && isMouseOver) {
                    GuiComponent.blit(matrixStack, left, top, 96.0F, 32.0F, 32, 32, 256, 256);
                } else {
                    GuiComponent.blit(matrixStack, left, top, 96.0F, 0.0F, 32, 32, 256, 256);
                }
            }

            if (parentScreen.canMoveDown(this)) {
                if (i < 32 && i > 16 && j > 16 && isMouseOver) {
                    GuiComponent.blit(matrixStack, left, top, 64.0F, 32.0F, 32, 32, 256, 256);
                } else {
                    GuiComponent.blit(matrixStack, left, top, 64.0F, 0.0F, 32, 32, 256, 256);
                }
            }

            minecraft.font.draw(matrixStack, minecraft.font.split(name, width - 36 - 45).get(0), left + 36, top, 0xFFFFFF);

            if (option.getClass().isAnnotationPresent(SettingsContainer.class)) {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.enableDepthTest();
                if (i > width - 40 && j > 6 && j < 26 && i < width - 20) {
                    GuiComponent.blit(matrixStack, left + width - 40, top + 6, 0, 86, 10, 20, 256, 256);
                    GuiComponent.blit(matrixStack, left + width - 30, top + 6, 200 - 10, 86, 10, 20, 256, 256);
                } else {
                    GuiComponent.blit(matrixStack, left + width - 40, top + 6, 0, 66, 10, 20, 256, 256);
                    GuiComponent.blit(matrixStack, left + width - 30, top + 6, 200 - 10, 66, 10, 20, 256, 256);
                }
                drawCenteredString(matrixStack, minecraft.font, "⚙", left + width - 30, top + 12, 0xFFFFFF);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            double d = mouseX - (double)this.parent.getRowLeft();
            double e = mouseY - (double)this.parent.getRowTop(this.parent.children().indexOf(this));
            if (d <= 32.0D) {
                if (d < 16.0D) {
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    this.parentScreen.unselect(this);
                    return true;
                }

                if (d > 16.0D && e < 16.0D && this.parentScreen.canMoveUp(this)) {
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    this.parentScreen.moveUp(this);
                    return true;
                }

                if (d > 16.0D && e > 16.0D && this.parentScreen.canMoveDown(this)) {
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    this.parentScreen.moveDown(this);
                    return true;
                }
            }
            if (d > parent.getRowWidth() - 20 && d < parent.getRowWidth() && e >= 6d && e <= 26d && option.getClass().isAnnotationPresent(SettingsContainer.class)) {
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                minecraft.setScreen(new SettingScreen(new TranslatableComponent(option.getClass().getAnnotation(SettingsContainer.class).value()), (Screen) parentScreen, option));
                return true;
            }
            return false;
        }

    }
}