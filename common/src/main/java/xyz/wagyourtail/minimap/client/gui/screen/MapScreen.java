package xyz.wagyourtail.minimap.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import xyz.wagyourtail.minimap.WagYourMinimap;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.api.client.MinimapClientEvents;
import xyz.wagyourtail.minimap.api.config.MinimapClientConfig;
import xyz.wagyourtail.minimap.api.config.layers.AbstractLayerOptions;
import xyz.wagyourtail.minimap.client.gui.image.AbstractImageStrategy;
import xyz.wagyourtail.minimap.client.gui.screen.renderer.ScreenMapRenderer;
import xyz.wagyourtail.minimap.client.gui.screen.settings.SettingsScreen;
import xyz.wagyourtail.minimap.client.gui.screen.widget.InteractMenu;
import xyz.wagyourtail.minimap.client.gui.screen.widget.MenuButton;
import xyz.wagyourtail.minimap.map.MapServer;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.map.chunkdata.parts.SurfaceDataPart;

import java.util.*;
import java.util.List;

public class MapScreen extends Screen {
    private static final ResourceLocation settings_tex = new ResourceLocation(WagYourMinimap.MOD_ID, "textures/gui/setting_icon.png");
    private static final ResourceLocation waypoint_tex = new ResourceLocation(WagYourMinimap.MOD_ID, "textures/gui/waypoint_icon.png");
    private static final ResourceLocation menu_end_tex = new ResourceLocation(WagYourMinimap.MOD_ID, "textures/gui/menu_end.png");
    private static final ResourceLocation menu_tex = new ResourceLocation(WagYourMinimap.MOD_ID, "textures/gui/menu.png");

    private int menuHeight;
    public ScreenMapRenderer renderer;
    public InteractMenu interact;

    public MapScreen() {
        super(new TranslatableComponent("gui.wagyourminimap.title"));
    }

    @Override
    public <T extends GuiEventListener & Widget & NarratableEntry> T addRenderableWidget(T widget) {
        return super.addRenderableWidget(widget);
    }

    @Override
    public void removeWidget(GuiEventListener guiEventListener) {
        super.removeWidget(guiEventListener);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean consumed = super.mouseClicked(mouseX, mouseY, button);
        if (interact != null) {
            interact.remove();
            interact = null;
        }
        if (!consumed && button == 1) {
            float x = (float) (renderer.topX + renderer.xDiam * mouseX / width);
            float z = (float) (renderer.topZ + renderer.zDiam * mouseY / height);


            MapServer.MapLevel level = MinimapClientApi.getInstance().getMapLevel(minecraft.level);
            if (level != null) {
                ChunkData chk = ChunkLocation.locationForChunkPos(level, ((int) x) >> 4, ((int) z) >> 4).get();
                SurfaceDataPart chunk = chk.getData(SurfaceDataPart.class).orElse(null);
                SurfaceDataPart south = chk.south().get().getData(SurfaceDataPart.class).orElse(null);
                SurfaceDataPart north = chk.north().get().getData(SurfaceDataPart.class).orElse(null);
                if (chunk != null && south != null && north != null) {
                    int[] above = new int[16];
                    int[] top = new int[16];
                    int[] bottom = new int[16];
                    int[] below = new int[16];
                    for (int i = 0; i < 16; i++) {
                        above[i] = north.heightmap[SurfaceDataPart.blockPosToIndex(i, 15)];
                        top[i] = chunk.heightmap[SurfaceDataPart.blockPosToIndex(i, 0)];
                        bottom[i] = chunk.heightmap[SurfaceDataPart.blockPosToIndex(i, 15)];
                        below[i] = south.heightmap[SurfaceDataPart.blockPosToIndex(i, 0)];
                    }
                    System.out.println(Arrays.toString(above));
                    System.out.println(Arrays.toString(top));
                    System.out.println(Arrays.toString(bottom));
                    System.out.println(Arrays.toString(below));
                }
            }

            interact = new InteractMenu(this, x, z, mouseX, mouseY);
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        boolean consumed = super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        if (button == 1) return true;
        if (!consumed) {
            renderer.moveCenter(renderer.center.subtract((dragX / renderer.chunkWidth) * 16, 0, (dragY / renderer.chunkWidth) * 16));
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
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(poseStack);

        renderer.renderMinimap(poseStack, mouseX, mouseY);

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
    protected void init() {
        super.init();

        renderer = new ScreenMapRenderer();
//        renderer.setOverlays(Arrays.stream(MinimapClientApi.getInstance().getConfig().get(MinimapClientConfig.class).fullscreenMapStyle.overlays).map(e -> e.compileOverlay(renderer)).toArray(AbstractMapOverlayRenderer[]::new));
        renderer.setRenderLayers(Arrays.stream(MinimapClientApi.getInstance().getConfig().get(MinimapClientConfig.class).fullscreenMapStyle.layers).map(AbstractLayerOptions::compileLayer).toArray(AbstractImageStrategy[]::new));
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

        MinimapClientEvents.FULLSCREEN_MENU.invoker().onPopulate(buttonList);

        setupMenu(buttonList);

    }

    private void setupMenu(List<MenuButton> buttons) {
        int i = height / 2 - buttons.size() * 30 + 5;
        for (MenuButton btn : buttons) {
            btn.x = 2;
            btn.y = i;
            i += 35;
            addRenderableWidget(btn);
        }
        menuHeight = (buttons.size() + 1) * 35;
    }

    private static void drawTex(PoseStack pose, int x1, int y1, int w, int h, float minU, float maxU, float minV, float maxV) {
        Matrix4f matrix = pose.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix, (float) x1, (float) (y1 + h), 0f).uv(minU, maxV).endVertex();
        bufferBuilder.vertex(matrix, (float) (x1 + w), (float) (y1 + h), 0f).uv(maxU, maxV).endVertex();
        bufferBuilder.vertex(matrix, (float) (x1 + w), (float) y1, 0f).uv(maxU, minV).endVertex();
        bufferBuilder.vertex(matrix, (float) x1, (float) y1, 0f).uv(minU, minV).endVertex();
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);
    }

}
