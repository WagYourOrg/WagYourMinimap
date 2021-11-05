package xyz.wagyourtail.minimap.client.gui.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import xyz.wagyourtail.minimap.WagYourMinimap;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.client.gui.renderer.AbstractMapRenderer;
import xyz.wagyourtail.minimap.client.gui.screen.WaypointListScreen;
import xyz.wagyourtail.minimap.waypoint.Waypoint;

public class WaypointList extends ObjectSelectionList<WaypointList.WaypointListEntry> {
    private final WaypointListScreen screen;
    private final Minecraft mc;


    public WaypointList(WaypointListScreen screen, Minecraft minecraft, int i, int j, int k, int l, int m) {
        super(minecraft, i, j, k, l, m);
        this.screen = screen;
        this.mc = minecraft;
        refreshEntries();
    }

    public void refreshEntries() {
        this.clearEntries();
        //TODO: add sorting
        for (Waypoint point : MinimapApi.getInstance().getMapServer().waypoints.getAllWaypoints()) {
            addEntry(new WaypointListEntry(screen, point));
        }
    }

    public void setSelected(@Nullable WaypointListEntry entry) {
        super.setSelected(entry);
        this.screen.onSelectedChange();
    }

    @Override
    public int getRowWidth() {
        return 400;
    }

    public static class WaypointListEntry extends ObjectSelectionList.Entry<WaypointListEntry> {
        private static final ResourceLocation waypoint_tex = new ResourceLocation(WagYourMinimap.MOD_ID, "textures/waypoint.png");

        private final Minecraft mc;
        private final WaypointListScreen screen;
        public Waypoint point;


        public WaypointListEntry(WaypointListScreen screen, Waypoint point) {
            this.screen = screen;
            this.point = point;
            this.mc = Minecraft.getInstance();
        }

        @Override
        public Component getNarration() {
            return TextComponent.EMPTY;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.screen.setSelected(this);
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public void render(PoseStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            RenderSystem.setShaderTexture(0, waypoint_tex);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            int abgr = 0xFF000000 | point.colB << 0x10 | point.colG << 0x8 | point.colR & 255;
            AbstractMapRenderer.drawTexCol(poseStack, left + 1, top + 1, height - 2, height - 2, 0, 0, 1, 1, abgr);
            mc.font.draw(poseStack, mc.font.plainSubstrByWidth(point.name, width - 100), left + height + 3, top + 1, 0xFFFFFF);
            RenderSystem.disableBlend();
        }
    }
}