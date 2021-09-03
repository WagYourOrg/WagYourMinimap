package xyz.wagyourtail.minimap.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import xyz.wagyourtail.minimap.WagYourMinimap;
import xyz.wagyourtail.minimap.api.client.MinimapClientEvents;
import xyz.wagyourtail.minimap.client.gui.screen.renderer.ScreenMapRenderer;
import xyz.wagyourtail.minimap.client.gui.screen.settings.SettingsScreen;
import xyz.wagyourtail.minimap.client.gui.screen.widget.MenuButton;

import java.util.ArrayList;
import java.util.List;

public class MapScreen extends Screen {
    private static final ResourceLocation settings_tex = new ResourceLocation(WagYourMinimap.MOD_ID, "textures/gui/setting_icon.png");
    private static final ResourceLocation waypoint_tex = new ResourceLocation(WagYourMinimap.MOD_ID, "textures/gui/waypoint_icon.png");
    private static final ResourceLocation menu_end_tex = new ResourceLocation(WagYourMinimap.MOD_ID, "textures/gui/menu_end.png");
    private static final ResourceLocation menu_tex = new ResourceLocation(WagYourMinimap.MOD_ID, "textures/gui/menu.png");

    private ScreenMapRenderer renderer;

    private int menuHeight;

    public MapScreen() {
        super(new TranslatableComponent("gui.wagyourminimap.title"));
    }

    @Override
    protected void init() {
        super.init();

        renderer = new ScreenMapRenderer();
        renderer.computeDimensions(width, height);
        renderer.moveCenter(minecraft.player.position());

        List<MenuButton> buttonList = new ArrayList<>();

        buttonList.add(new MenuButton(new TranslatableComponent("gui.wagyourminimap.settings"), settings_tex, (btn) -> {
            minecraft.setScreen(new SettingsScreen(this));
        }));

//        buttonList.add(new MenuButton(new TranslatableComponent("gui.wagyourminimap.test"), waypoint_tex, null));
//        buttonList.add(new MenuButton(new TranslatableComponent("gui.wagyourminimap.test"), waypoint_tex, null));

        buttonList.add(new MenuButton(new TranslatableComponent("gui.wagyourminimap.waypoints"), waypoint_tex, (btn) -> {
            minecraft.setScreen(new WaypointsScreen(this));
        }));

        MinimapClientEvents.EVENT_MENU_BUTTONS.invoker().onPopulate(buttonList);

        setupMenu(buttonList);

    }

    private void setupMenu(List<MenuButton> buttons) {
        int i = height / 2 - buttons.size() * 30 + 5;
        for (MenuButton btn : buttons) {
            btn.x = 2;
            btn.y = i;
            i += 50;
            addRenderableWidget(btn);
        }
        menuHeight = (buttons.size() + 1) * 50;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean consumed = super.mouseClicked(mouseX, mouseY, button);
        if (!consumed && button == 1) {
            //on right click map
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        boolean consumed = super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        if (!consumed) {
            renderer.moveCenter(renderer.center.subtract((dragX / renderer.chunkWidth) * 16, 0, (dragY / renderer.chunkWidth) * 16));
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        boolean consumed = super.mouseScrolled(mouseX, mouseY, delta);
        if (!consumed) {
            renderer.changeZoom(renderer.blockRadius - (int) delta);
        }
        return true;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(poseStack);

        renderer.renderMinimap(poseStack, null, 0, minecraft.player.position(), minecraft.player.getYRot());

        RenderSystem.setShaderTexture(0, menu_end_tex);
        int menuTop = height / 2 - menuHeight / 2 - 15;
        drawTex(poseStack, 0, menuTop, 60, 60, 0, 1, 0, 1);
        int menuBottom = height / 2 + menuHeight / 2 - 65;
        drawTex(poseStack, 0, menuBottom, 60, 60, 0, 1, 1, 0);
        RenderSystem.setShaderTexture(0, menu_tex);
        drawTex(poseStack, 0, menuTop + 60, 60, menuHeight - 110, 0, 1, 0, 1);
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    private static void drawTex(PoseStack pose, int x1, int y1, int w, int h, float minU, float maxU, float minV, float maxV) {
        Matrix4f matrix = pose.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix, (float)x1, (float)(y1 + h), 0f).uv(minU, maxV).endVertex();
        bufferBuilder.vertex(matrix, (float)(x1 + w), (float)(y1 + h), 0f).uv(maxU, maxV).endVertex();
        bufferBuilder.vertex(matrix, (float)(x1 + w), (float)y1, 0f).uv(maxU, minV).endVertex();
        bufferBuilder.vertex(matrix, (float)x1, (float)y1, 0f).uv(minU, minV).endVertex();
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);
    }

}
