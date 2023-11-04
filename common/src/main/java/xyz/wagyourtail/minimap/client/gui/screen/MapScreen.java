package xyz.wagyourtail.minimap.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import xyz.wagyourtail.minimap.WagYourMinimap;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.api.client.MinimapClientEvents;
import xyz.wagyourtail.minimap.api.client.config.MinimapClientConfig;
import xyz.wagyourtail.minimap.client.gui.screen.map.ScreenMapRenderer;
import xyz.wagyourtail.minimap.client.gui.screen.widget.InteractMenu;
import xyz.wagyourtail.minimap.client.gui.screen.widget.MenuButton;

import java.util.ArrayList;
import java.util.List;

public class MapScreen extends Screen {
    private static final ResourceLocation settings_tex = new ResourceLocation(
        WagYourMinimap.MOD_ID,
        "textures/gui/setting_icon.png"
    );
    private static final ResourceLocation waypoint_tex = new ResourceLocation(
        WagYourMinimap.MOD_ID,
        "textures/gui/waypoint_icon.png"
    );
    private static final ResourceLocation menu_end_tex = new ResourceLocation(
        WagYourMinimap.MOD_ID,
        "textures/gui/menu_end.png"
    );
    private static final ResourceLocation menu_tex = new ResourceLocation(
        WagYourMinimap.MOD_ID,
        "textures/gui/menu.png"
    );

    public static int SELECT_REGION_BORDER_COLOR = 0xFFFF0000;
    public static int SELECT_REGION_COLOR = 0x4FFF0000;

    private int menuHeight;
    private double dragStartX, dragStartY;
    private double dragEndX, dragEndY;
    public ScreenMapRenderer renderer;
    public InteractMenu interact;

    public MapScreen() {
        super(net.minecraft.network.chat.Component.translatable("gui.wagyourminimap.title"));
    }

    private long clickStartTime = 0;

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        clickStartTime = System.currentTimeMillis();
        dragStartX = mouseX;
        dragStartY = mouseY;
        dragEndX = mouseX;
        dragEndY = mouseY;
        boolean consumed = super.mouseClicked(mouseX, mouseY, button);
        if (interact != null) {
            interact.remove();
            interact = null;
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean consumed = super.mouseReleased(mouseX, mouseY, button);
        if (!consumed && button == 1) {
            float sX = (float) (renderer.topX + renderer.xDiam * dragStartX / width);
            float sZ = (float) (renderer.topZ + renderer.zDiam * dragStartY / height);
            float eX = (float) (renderer.topX + renderer.xDiam * mouseX / width);
            float eZ = (float) (renderer.topZ + renderer.zDiam * mouseY / height);
            interact = new InteractMenu(this, dragStartX, dragStartY, mouseX, mouseY, sX, sZ, eX, eZ);
            return true;
        }
        return consumed;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        boolean consumed = super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        if (button == 1) {
            if (System.currentTimeMillis() - clickStartTime < renderer.dragSelectDelay) {
                return true;
            }
            dragEndX = mouseX;
            dragEndY = mouseY;
            return true;
        }
        if (!consumed) {
            renderer.moveCenter(renderer.center.subtract(
                (dragX / renderer.chunkWidth) * 16,
                0,
                (dragY / renderer.chunkWidth) * 16
            ));
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        boolean consumed = super.mouseScrolled(mouseX, mouseY, delta);
        if (!consumed) {
            renderer.changeZoom(renderer.blockRadius - (int) delta * 5);
        }
        return true;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (codePoint == '-') {
            renderer.changeZoom(renderer.blockRadius + 64);
            return true;
        } else if (codePoint == '=') {
            renderer.changeZoom(renderer.blockRadius - 64);
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void render(GuiGraphics poseStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(poseStack);

        renderer.renderMinimap(poseStack, mouseX, mouseY);

        if (dragStartX != dragEndX && dragStartY != dragEndY) {
            poseStack.fill((int) dragStartX, (int) dragStartY, (int) dragEndX, (int) dragEndY, SELECT_REGION_COLOR);
            poseStack.fill(
                (int) dragStartX,
                (int) dragStartY + 1,
                (int) dragStartX + 1,
                (int) dragEndY + 1,
                SELECT_REGION_BORDER_COLOR
            );
            poseStack.fill(
                (int) dragStartX,
                (int) dragStartY,
                (int) dragEndX,
                (int) dragStartY + 1,
                SELECT_REGION_BORDER_COLOR
            );
            poseStack.fill(
                (int) dragEndX,
                (int) dragStartY + 1,
                (int) dragEndX + 1,
                (int) dragEndY + 1,
                SELECT_REGION_BORDER_COLOR
            );
            poseStack.fill(
                (int) dragStartX,
                (int) dragEndY,
                (int) dragEndX,
                (int) dragEndY + 1,
                SELECT_REGION_BORDER_COLOR
            );
        }

        if (interact != null) {
            interact.render(poseStack, mouseX, mouseY, partialTicks);
        }

        RenderSystem.setShaderTexture(0, menu_end_tex);
        int menuTop = height / 2 - menuHeight / 2 - 15;
        drawTex(poseStack, 0, menuTop, 45, 45, 0, 1, 0, 1);
        int menuBottom = height / 2 + menuHeight / 2 - 75;
        drawTex(poseStack, 0, menuBottom, 45, 45, 0, 1, 1, 0);
        RenderSystem.setShaderTexture(0, menu_tex);
        drawTex(poseStack, 0, menuTop + 45, 45, menuHeight - 110, 0, 1, 0, 1);

        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public <T extends GuiEventListener & Renderable  & NarratableEntry> T addRenderableWidget(T widget) {
        return super.addRenderableWidget(widget);
    }

    @Override
    public void removeWidget(GuiEventListener guiEventListener) {
        super.removeWidget(guiEventListener);
    }

    @Override
    protected void init() {
        super.init();

        renderer = MinimapClientApi.getInstance().getConfig().get(MinimapClientConfig.class).fullscreenRenderer;
        renderer.computeDimensions(width, height);
        renderer.moveCenter(minecraft.player.position());

        List<MenuButton> buttonList = new ArrayList<>();

        buttonList.add(new MenuButton(net.minecraft.network.chat.Component.translatable("gui.wagyourminimap.settings"), settings_tex, (btn) -> {
            minecraft.setScreen(new SettingsScreen(this));
        }));

        buttonList.add(new MenuButton(
            net.minecraft.network.chat.Component.translatable("gui.wagyourminimap.waypoints"),
            waypoint_tex,
            (btn) -> {
                minecraft.setScreen(new WaypointListScreen(this));
            }
        ));

        MinimapClientEvents.FULLSCREEN_MENU.invoker().onPopulate(buttonList);

        setupMenu(buttonList);

    }

    private void setupMenu(List<MenuButton> buttons) {
        int i = height / 2 - buttons.size() * 30 + 5;
        for (MenuButton btn : buttons) {
            btn.setX(2);
            btn.setY(i);
            i += 35;
            addRenderableWidget(btn);
        }
        menuHeight = (buttons.size() + 1) * 35;
    }

    private static void drawTex(GuiGraphics pose, int x1, int y1, int w, int h, float minU, float maxU, float minV, float maxV) {
        Matrix4f matrix = pose.pose().last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix, (float) x1, (float) (y1 + h), 0f).uv(minU, maxV).endVertex();
        bufferBuilder.vertex(matrix, (float) (x1 + w), (float) (y1 + h), 0f).uv(maxU, maxV).endVertex();
        bufferBuilder.vertex(matrix, (float) (x1 + w), (float) y1, 0f).uv(maxU, minV).endVertex();
        bufferBuilder.vertex(matrix, (float) x1, (float) y1, 0f).uv(minU, minV).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
    }

}
