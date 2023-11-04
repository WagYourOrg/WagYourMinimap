package xyz.wagyourtail.minimap.client.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.api.client.MinimapClientEvents;
import xyz.wagyourtail.minimap.api.client.config.CurrentServerConfig;
import xyz.wagyourtail.minimap.client.gui.screen.widget.WaypointList;

import java.util.ArrayList;
import java.util.List;

public class WaypointListScreen extends Screen {
    private final Screen parent;
    private final List<Button> buttons = new ArrayList<>();
    private final List<Button> waypointNotNullButtons = new ArrayList<>();
    private WaypointList waypointListWidget;

    protected WaypointListScreen(Screen parent) {
        super(net.minecraft.network.chat.Component.translatable("gui.wagyourminimap.waypoints"));
        this.parent = parent;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(poseStack);

        this.waypointListWidget.render(poseStack, mouseX, mouseY, partialTicks);

        drawCenteredString(poseStack, font, title, width / 2, 8, 0xFFFFFF);

        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    protected void init() {
        super.init();
        buttons.clear();
        waypointNotNullButtons.clear();

        waypointListWidget = new WaypointList(this, minecraft, width, height, 32, height - 64, 16);
        addWidget(waypointListWidget);

        buttons.addAll(List.of(new Button.Builder(
            net.minecraft.network.chat.Component.translatable("gui.wagyourminimap.waypoints.add"),
            (button) -> {
                assert minecraft.player != null;
                minecraft.setScreen(WaypointEditScreen.createNewFromPos(
                    this,
                    new BlockPos(minecraft.player.blockPosition()).above()
                ));
            }
        ).bounds(
            0,
            0,
            0,
            20
        ).build(), new Button.Builder(net.minecraft.network.chat.Component.translatable("gui.wagyourminimap.waypoints.reload"), (button) -> {
            refreshEntries();
        }).bounds(0, 0, 0, 20).build(), new Button.Builder(net.minecraft.network.chat.Component.translatable("gui.wagyourminimap.close"), (button) -> {
            onClose();
        }).bounds(0, 0, 0, 20).build()));


        waypointNotNullButtons.addAll(List.of(
            new Button.Builder(
                net.minecraft.network.chat.Component.translatable("gui.wagyourminimap.waypoints.edit"),
                (button) -> {
                    WaypointList.WaypointListEntry selected = getSelected();
                    if (selected != null) {
                        assert minecraft != null;
                        minecraft.setScreen(new WaypointEditScreen(this, selected.point));
                    }
                }
            ).bounds(
                0,
                0,
                0,
                20
            ).build(),
            new Button.Builder(net.minecraft.network.chat.Component.translatable("gui.wagyourminimap.waypoints.delete"), (button) -> {
                WaypointList.WaypointListEntry selected = getSelected();
                if (selected != null) {
                    MinimapApi.getInstance().getMapServer().waypoints.removeWaypoint(selected.point);
                    refreshEntries();
                }
            }).bounds(0, 0, 0, 20).build(),
            new Button.Builder(net.minecraft.network.chat.Component.translatable("gui.wagyourminimap.waypoints.enable"), (button) -> {
                WaypointList.WaypointListEntry selected = getSelected();
                if (selected != null) {
                    selected.toggleEnabled();
                }
            }).bounds(0, 0, 0, 20).build(),
            new Button.Builder(net.minecraft.network.chat.Component.translatable("gui.wagyourminimap.waypoints.teleport"), (button) -> {
                WaypointList.WaypointListEntry selected = getSelected();
                if (selected != null) {
                    assert minecraft != null;
                    BlockPos pos = selected.point.posForCoordScale(minecraft.level.dimensionType().coordinateScale());
                    MinimapClientApi.getInstance().sendTp(pos.getX(), pos.getY(), pos.getZ());
                }
            }).bounds(0, 0, 0, 20).build()
        ));

        MinimapClientEvents.WAYPOINT_LIST_MENU.invoker().onPopulate(this, buttons, waypointNotNullButtons);

        int waypointNotNullButtonsWidth = Math.min(100, width / waypointNotNullButtons.size());
        int offset = width / 2 - (waypointNotNullButtonsWidth * waypointNotNullButtons.size()) / 2;

        for (int i = 0; i < waypointNotNullButtons.size(); i++) {
            Button button = waypointNotNullButtons.get(i);
            button.setX(i * waypointNotNullButtonsWidth + offset);
            button.setY(height - 50);
            button.active = false;
            button.setWidth(waypointNotNullButtonsWidth - 5);
            addRenderableWidget(button);
        }

        int buttonsWidth = Math.min(100, width / buttons.size());
        offset = width / 2 - (buttons.size() * buttonsWidth) / 2;

        for (int i = 0; i < buttons.size(); i++) {
            Button button = buttons.get(i);
            button.setX(i * buttonsWidth + offset);
            button.setY(height - 25);
            button.setWidth(buttonsWidth - 5);
            addRenderableWidget(button);
        }

    }

    public WaypointList.WaypointListEntry getSelected() {
        return this.waypointListWidget.getSelected();
    }

    public void setSelected(@Nullable WaypointList.WaypointListEntry entry) {
        this.waypointListWidget.setSelected(entry);
        this.onSelectedChange();
    }

    public void onSelectedChange() {
        WaypointList.WaypointListEntry selected = this.waypointListWidget.getSelected();
        if (selected != null) {
            for (Button button : waypointNotNullButtons) {
                button.active = true;
            }
        } else {
            for (Button button : waypointNotNullButtons) {
                button.active = false;
            }
        }
    }

    public void refreshEntries() {
        this.waypointListWidget.refreshEntries();
        setSelected(null);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        WaypointList.WaypointListEntry selected = getSelected();
        switch (keyCode) {
            case GLFW.GLFW_KEY_DELETE: {
                if (selected != null) {
                    MinimapApi.getInstance().getMapServer().waypoints.removeWaypoint(selected.point);
                    refreshEntries();
                }
                return true;
            }
            case GLFW.GLFW_KEY_ENTER: {
                if (selected != null) {
                    // edit
                    assert minecraft != null;
                    minecraft.setScreen(new WaypointEditScreen(this, selected.point));
                }
                return true;
            }
            case GLFW.GLFW_KEY_DOWN: {
                int idx = this.waypointListWidget.children().indexOf(selected);
                if (idx < this.waypointListWidget.children().size() - 1) {
                    setSelected(this.waypointListWidget.children().get(idx + 1));
                }
                return true;
            }
            case GLFW.GLFW_KEY_UP: {
                int idx = this.waypointListWidget.children().indexOf(selected);
                if (idx > 0) {
                    setSelected(this.waypointListWidget.children().get(idx - 1));
                }
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

}
