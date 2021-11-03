package xyz.wagyourtail.minimap.client.gui.screen.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.client.gui.screen.WaypointsScreen;
import xyz.wagyourtail.minimap.waypoint.Waypoint;

public class WaypointList extends ObjectSelectionList<WaypointList.WaypointListEntry> {
    private final WaypointsScreen screen;


    public WaypointList(WaypointsScreen screen, Minecraft minecraft, int i, int j, int k, int l, int m) {
        super(minecraft, i, j, k, l, m);
        this.screen = screen;

    }

    public void refreshEntries() {
        this.clearEntries();
        //TODO: add sorting
        for (Waypoint point : MinimapApi.getInstance().getMapServer().waypoints.getAllWaypoints()) {
            addEntry(new WaypointListEntry(point));
        }
    }

    public static class WaypointListEntry extends ObjectSelectionList.Entry<WaypointListEntry> {
        public Waypoint point;

        public WaypointListEntry(Waypoint point) {
            this.point = point;
        }

        @Override
        public Component getNarration() {
            return null;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {

            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public void render(PoseStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {

        }
    }
}
