package xyz.wagyourtail.minimap.client.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.Nullable;
import xyz.wagyourtail.minimap.api.MinimapApi;
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
        super(new TranslatableComponent("gui.wagyourminimap.waypoints"));
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

        buttons.addAll(List.of(new Button(
            0,
            0,
            0,
            20,
            new TranslatableComponent("gui.wagyourminimap.waypoints.add"),
            (button) -> {
                assert minecraft.player != null;
                minecraft.setScreen(WaypointEditScreen.createNewFromPos(
                    this,
                    new BlockPos(minecraft.player.getPosition(0)).above()
                ));
            }
        ), new Button(0, 0, 0, 20, new TranslatableComponent("gui.wagyourminimap.waypoints.reload"), (button) -> {
            refreshEntries();
        }), new Button(0, 0, 0, 20, new TranslatableComponent("gui.wagyourminimap.close"), (button) -> {
            onClose();
        })));


        waypointNotNullButtons.addAll(List.of(
            new Button(
                0,
                0,
                0,
                20,
                new TranslatableComponent("gui.wagyourminimap.waypoints.edit"),
                (button) -> {
                    WaypointList.WaypointListEntry selected = getSelected();
                    if (selected != null) {
                        assert minecraft != null;
                        minecraft.setScreen(new WaypointEditScreen(this, selected.point));
                    }
                }
            ),
            new Button(0, 0, 0, 20, new TranslatableComponent("gui.wagyourminimap.waypoints.delete"), (button) -> {
                WaypointList.WaypointListEntry selected = getSelected();
                if (selected != null) {
                    MinimapApi.getInstance().getMapServer().waypoints.removeWaypoint(selected.point);
                    refreshEntries();
                }
            }),
            new Button(0, 0, 0, 20, new TranslatableComponent("gui.wagyourminimap.waypoints.enable"), (button) -> {
                WaypointList.WaypointListEntry selected = getSelected();
                if (selected != null) {
                    selected.toggleEnabled();
                }
            }),
            new Button(0, 0, 0, 20, new TranslatableComponent("gui.wagyourminimap.waypoints.teleport"), (button) -> {
                WaypointList.WaypointListEntry selected = getSelected();
                if (selected != null) {
                    assert minecraft != null;
                    BlockPos pos = selected.point.posForCoordScale(minecraft.level.dimensionType().coordinateScale());
                    this.sendMessage(MinimapApi.getInstance()
                        .getConfig()
                        .get(CurrentServerConfig.class)
                        .getTpCommand()
                        .replace("%player", minecraft.player.getGameProfile().getName())
                        .replace("%x", Integer.toString(pos.getX()))
                        .replace("%y", Integer.toString(pos.getY()))
                        .replace("%z", Integer.toString(pos.getZ())));
                }
            })
        ));

        MinimapClientEvents.WAYPOINT_LIST_MENU.invoker().onPopulate(this, buttons, waypointNotNullButtons);

        int waypointNotNullButtonsWidth = Math.min(100, width / waypointNotNullButtons.size());
        int offset = width / 2 - (waypointNotNullButtonsWidth * waypointNotNullButtons.size()) / 2;

        for (int i = 0; i < waypointNotNullButtons.size(); i++) {
            Button button = waypointNotNullButtons.get(i);
            button.x = i * waypointNotNullButtonsWidth + offset;
            button.y = height - 50;
            button.active = false;
            button.setWidth(waypointNotNullButtonsWidth - 5);
            addRenderableWidget(button);
        }

        int buttonsWidth = Math.min(100, width / buttons.size());
        offset = width / 2 - (buttons.size() * buttonsWidth) / 2;

        for (int i = 0; i < buttons.size(); i++) {
            Button button = buttons.get(i);
            button.x = i * buttonsWidth + offset;
            button.y = height - 25;
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

}
