package xyz.wagyourtail.minimap.client.gui.screen.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.api.client.MinimapClientEvents;
import xyz.wagyourtail.minimap.client.gui.screen.MapScreen;
import xyz.wagyourtail.minimap.client.gui.screen.WaypointEditScreen;
import xyz.wagyourtail.minimap.client.gui.screen.widget.InteractMenuButton;
import xyz.wagyourtail.minimap.map.MapLevel;
import xyz.wagyourtail.minimap.map.MapServer;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.waypoint.Waypoint;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InteractMenu extends GuiComponent implements Widget {
    public static int backround_color = 0xFF3F3F74;
    public static String teleport_command = "/tp";
    protected static final Minecraft minecraft = Minecraft.getInstance();
    public final MapScreen parent;
    public final float x, z;
    public final double topX, topY;
    public final List<Waypoint> waypoints;
    public final Map<String, List<InteractMenuButton>> buttons = new LinkedHashMap<>();
    public int totalBtns = 0;

    public InteractMenu(MapScreen parent, float x, float z, double mouseX, double mouseY) {
        this.parent = parent;
        this.x = x;
        this.z = z;
        this.topX = mouseX;
        this.topY = mouseY;
        this.waypoints = waypointsNearPos((int)x, (int)z);
        this.init();
    }

    private void init() {
        MapLevel level = MinimapApi.getInstance().getMapLevel(minecraft.level);
        int y = 0;
        if (level != null) {
            ChunkData chunk = level.getChunk(ChunkLocation.locationForChunkPos(level, (int)x >> 4, (int)z >> 4));
            if (chunk != null) {
                y = chunk.heightmap[ChunkData.blockPosToIndex((int)x, (int)z)];
            }
        }

        Vec3 pos = new Vec3(x, y, z);
        buttons.put(I18n.get("gui.wagyourminimap.general"), buttonsForPos(pos));

        for (Waypoint waypoint : waypoints) {
            buttons.put(I18n.get("gui.wagyourminimap.waypoint") + ": " + waypoint.name(), buttonsForWaypoint(waypoint));
        }

        MinimapClientEvents.FULLSCREEN_INTERACT_MENU.invoker().onPopulate(this);

        int leftX = (int)topX;
        int currentTopY = (int) topY;

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

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        fill(poseStack, (int)topX - 2, (int)topY - 2, (int)topX + 102, (int)topY + totalBtns * InteractMenuButton.btnHeight + buttons.size() * 6 + 2, backround_color);
        int currentY = (int)topY;
        for (Map.Entry<String, List<InteractMenuButton>> group : buttons.entrySet()) {
            poseStack.pushPose();
            poseStack.translate(topX + 50, currentY, 0);
            poseStack.scale(.6f, .6f, 1);
            drawCenteredString(poseStack, minecraft.font, group.getKey(), 0, 0, 0x639BFF);
            poseStack.popPose();
            currentY += 6 + group.getValue().size() * InteractMenuButton.btnHeight;
        }
    }

    private List<InteractMenuButton> buttonsForPos(Vec3 pos) {
        List<InteractMenuButton> buttons = new ArrayList<>();
        buttons.add(new InteractMenuButton(new TranslatableComponent("gui.wagyourminimap.create_waypoint"), (btn) -> {
            int color = Color.HSBtoRGB((float)Math.random(), 1f, 1f);
            String[] dims;
            if (minecraft.level.dimension().equals(Level.OVERWORLD) || minecraft.level.dimension().equals(Level.NETHER)) {
                dims = new String[]{MapServer.getLevelName(Level.OVERWORLD), MapServer.getLevelName(Level.NETHER)};
            } else {
                dims = new String[]{MapServer.getLevelName(minecraft.level)};
            }
            minecraft.setScreen(new WaypointEditScreen(parent, new Waypoint((int)Math.floor(pos.x), (int)Math.floor(pos.y) + 1, (int)Math.floor(pos.z), (byte)((color >> 16) & 255), (byte)((color >> 8) & 255), (byte)(color & 255), "", new String[] {"default"}, dims, false)));
        }));

        buttons.add(new InteractMenuButton(new TranslatableComponent("gui.wagyourminimap.teleport_to"), (btn) -> {
            parent.sendMessage(String.format("%s %f %f %f", teleport_command, pos.x, pos.y + 1, pos.z));
        }));
        return buttons;
    }

    private List<InteractMenuButton> buttonsForWaypoint(Waypoint point) {
        List<InteractMenuButton> buttons = new ArrayList<>();
        buttons.add(new InteractMenuButton(new TranslatableComponent("gui.wagyourminimap.edit_waypoint"), (btn) -> {
            minecraft.setScreen(new WaypointEditScreen(parent, point));
        }));

        buttons.add(new InteractMenuButton(new TranslatableComponent("gui.wagyourminimap.teleport_to"), (btn) -> {
            parent.sendMessage(String.format("%s %d %d %d", teleport_command, point.posX(), point.posY(), point.posZ()));
        }));

        buttons.add(new InteractMenuButton(new TranslatableComponent("gui.wagyourminimap.delete_waypoint"), (btn) -> {
            MinimapApi.getInstance().getMapServer().waypoints.removeWaypoint(point);
        }));
        return buttons;
    }

    private List<Waypoint> waypointsNearPos(int x, int z) {
        float rad = 10f * 16f / parent.renderer.chunkWidth;
        return MinimapApi.getInstance().getMapServer().waypoints.getVisibleWaypoints().stream().filter(e -> new Vec3(e.posX(), 0, e.posZ()).distanceTo(new Vec3(x, 0, z)) <= rad).collect(Collectors.toList());
    }

    public void remove() {
        for (List<InteractMenuButton> value : buttons.values()) {
            value.forEach(parent::removeWidget);
        }
    }
}
