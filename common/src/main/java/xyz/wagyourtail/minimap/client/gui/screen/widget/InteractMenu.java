package xyz.wagyourtail.minimap.client.gui.screen.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.phys.Vec3;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.api.client.MinimapClientEvents;
import xyz.wagyourtail.minimap.api.client.config.CurrentServerConfig;
import xyz.wagyourtail.minimap.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.chunkdata.parts.SurfaceDataPart;
import xyz.wagyourtail.minimap.client.gui.screen.MapScreen;
import xyz.wagyourtail.minimap.client.gui.screen.WaypointEditScreen;
import xyz.wagyourtail.minimap.map.MapServer;
import xyz.wagyourtail.minimap.waypoint.Waypoint;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InteractMenu extends GuiComponent implements Widget {
    protected static final Minecraft minecraft = Minecraft.getInstance();
    public static int backround_color = 0xFF3F3F74;
    public final MapScreen parent;
    public final double startX, startY, endX, endY;
    public final float startXBlock, startZBlock, endXBlock, endZBlock;
    public final List<Waypoint> waypoints;
    public final Map<String, List<InteractMenuButton>> buttons = new LinkedHashMap<>();
    public int totalBtns = 0;

    public InteractMenu(MapScreen parent, double startX, double startY, double endX, double endY, float startXBlock, float startZBlock, float endXBlock, float endZBlock) {
        this.parent = parent;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.startXBlock = startXBlock;
        this.startZBlock = startZBlock;
        this.endXBlock = endXBlock;
        this.endZBlock = endZBlock;
        this.waypoints = waypointsInOrNear();
        this.init();
    }

    private void init() {
        assert minecraft.level != null;
        MapServer.MapLevel level = MinimapApi.getInstance().getMapServer().getLevelFor(minecraft.level);
        if (startX == endX && startY == endY) {
            int y = 0;
            if (level != null) {
                ChunkData chunk = ChunkLocation.locationForChunkPos(
                    level,
                    (int) startXBlock >> 4,
                    (int) startZBlock >> 4
                ).get();
                if (chunk != null) {
                    SurfaceDataPart surface = chunk.getData(SurfaceDataPart.class).orElse(null);
                    if (surface != null) {
                        y = surface.heightmap[SurfaceDataPart.blockPosToIndex((int) startXBlock, (int) startZBlock)];
                    }
                }
            }

            Vec3 pos = new Vec3(startXBlock, y, startZBlock);
            buttons.put(I18n.get("gui.wagyourminimap.general") + ": ", buttonsForPos(pos));
        } else {
            buttons.put(
                I18n.get("gui.wagyourminimap.general") + ": ",
                buttonsForRegion(new Vec3(startXBlock, 0, startZBlock), new Vec3(endXBlock, 0, endZBlock))
            );
        }


        for (Waypoint waypoint : waypoints) {
            buttons.put(I18n.get("gui.wagyourminimap.waypoint") + ": " + waypoint.name, buttonsForWaypoint(waypoint));
        }

        MinimapClientEvents.FULLSCREEN_INTERACT_MENU.invoker().onPopulate(this);

        int leftX = (int) endX;
        int currentTopY = (int) endY;

        for (Map.Entry<String, List<InteractMenuButton>> group : buttons.entrySet()) {
            currentTopY += 6;
            totalBtns += group.getValue().size();
            for (InteractMenuButton btn : group.getValue()) {
                btn.x = leftX;
                btn.y = currentTopY;
                parent.addRenderableWidget(btn);
                currentTopY += InteractMenuButton.btnHeight;
            }
        }
    }

    private List<InteractMenuButton> buttonsForPos(Vec3 pos) {
        List<InteractMenuButton> buttons = new ArrayList<>();
        buttons.add(new InteractMenuButton(
            new TranslatableComponent("gui.wagyourminimap.create_waypoint"),
            (btn) -> minecraft.setScreen(WaypointEditScreen.createNewFromPos(parent, new BlockPos(pos).above()))
        ));

        buttons.add(new InteractMenuButton(
            new TranslatableComponent("gui.wagyourminimap.teleport_to"),
            (btn) -> parent.sendMessage(MinimapApi.getInstance()
                .getConfig()
                .get(CurrentServerConfig.class)
                .getTpCommand()
                .replace("%player", minecraft.player.getGameProfile().getName())
                .replace("%x", Integer.toString((int) pos.x))
                .replace("%y", Integer.toString((int) pos.y + 1))
                .replace("%z", Integer.toString((int) pos.z)))
        ));
        return buttons;
    }

    private List<InteractMenuButton> buttonsForRegion(Vec3 start, Vec3 end) {
        List<InteractMenuButton> buttons = new ArrayList<>();
        buttons.add(new InteractMenuButton(
                new TranslatableComponent("gui.wagyourminimap.delete_all_waypoints"),
                (btn) -> {
                    for (Waypoint waypoint : waypoints) {
                        MinimapApi.getInstance().getMapServer().waypoints.removeWaypoint(waypoint);
                    }
                }
            )
        );
        return buttons;
    }

    private List<InteractMenuButton> buttonsForWaypoint(Waypoint point) {
        List<InteractMenuButton> buttons = new ArrayList<>();
        buttons.add(new InteractMenuButton(
            new TranslatableComponent("gui.wagyourminimap.edit_waypoint"),
            (btn) -> minecraft.setScreen(new WaypointEditScreen(parent, point))
        ));

        buttons.add(new InteractMenuButton(new TranslatableComponent("gui.wagyourminimap.teleport_to"), (btn) -> {
            BlockPos pos = point.posForCoordScale(minecraft.level.dimensionType().coordinateScale());
            parent.sendMessage(MinimapApi.getInstance()
                .getConfig()
                .get(CurrentServerConfig.class)
                .getTpCommand()
                .replace("%player", minecraft.player.getGameProfile().getName())
                .replace("%x", Integer.toString(pos.getX()))
                .replace("%y", Integer.toString(pos.getY()))
                .replace("%z", Integer.toString(pos.getZ())));
        }));

        buttons.add(new InteractMenuButton(
            new TranslatableComponent("gui.wagyourminimap.delete_waypoint"),
            (btn) -> MinimapApi.getInstance().getMapServer().waypoints.removeWaypoint(point)
        ));
        return buttons;
    }

    private List<Waypoint> waypointsInOrNear() {
        if (startX == endX && startY == endY) {
            float rad = 10f * 16f / parent.renderer.chunkWidth;
            double coordScale = minecraft.level.dimensionType().coordinateScale();
            return MinimapApi.getInstance().getMapServer().waypoints.getAllWaypoints().stream().filter(e -> {
                BlockPos pos = e.posForCoordScale(coordScale);
                return new Vec3(pos.getX(), 0, pos.getZ()).distanceTo(new Vec3(startXBlock, 0, startZBlock)) <= rad;
            }).collect(Collectors.toList());
        } else {
            return MinimapApi.getInstance().getMapServer().waypoints.getAllWaypoints().stream().filter(e -> {
                BlockPos pos = e.posForCoordScale(minecraft.level.dimensionType().coordinateScale());
                float x1, x2, z1, z2;
                if (startXBlock < endXBlock) {
                    x1 = startXBlock;
                    x2 = endXBlock;
                } else {
                    x1 = endXBlock;
                    x2 = startXBlock;
                }
                if (startZBlock < endZBlock) {
                    z1 = startZBlock;
                    z2 = endZBlock;
                } else {
                    z1 = endZBlock;
                    z2 = startZBlock;
                }
                return pos.getX() >= x1 && pos.getX() <= x2 && pos.getZ() >= z1 && pos.getZ() <= z2;
            }).collect(Collectors.toList());
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        fill(
            poseStack,
            (int) endX - 2,
            (int) endY - 2,
            (int) endX + 102,
            (int) endY + totalBtns * InteractMenuButton.btnHeight + buttons.size() * 6 + 2,
            backround_color
        );
        int currentY = (int) endY;
        for (Map.Entry<String, List<InteractMenuButton>> group : buttons.entrySet()) {
            poseStack.pushPose();
            poseStack.translate(endX + 50, currentY, 0);
            poseStack.scale(.6f, .6f, 1);
            drawCenteredString(poseStack, minecraft.font, group.getKey(), 0, 0, 0x639BFF);
            poseStack.popPose();
            currentY += 6 + group.getValue().size() * InteractMenuButton.btnHeight;
        }
    }

    public void remove() {
        for (List<InteractMenuButton> value : buttons.values()) {
            value.forEach(parent::removeWidget);
        }
    }

}
