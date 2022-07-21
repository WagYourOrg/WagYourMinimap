package xyz.wagyourtail.minimap.client.gui.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.Nullable;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.client.gui.AbstractMapRenderer;
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

    @Override
    public int getRowWidth() {
        return 400;
    }

    public void setSelected(@Nullable WaypointListEntry entry) {
        super.setSelected(entry);
        this.screen.onSelectedChange();
    }

    public static class WaypointListEntry extends ObjectSelectionList.Entry<WaypointListEntry> {
        private final Minecraft mc;
        private final WaypointListScreen screen;
        public Waypoint point;

        public Component name;


        public WaypointListEntry(WaypointListScreen screen, Waypoint point) {
            this.screen = screen;
            this.point = point;
            this.mc = Minecraft.getInstance();
            this.name = new TextComponent(point.name).withStyle(point.enabled ?
                new ChatFormatting[] {ChatFormatting.WHITE} :
                new ChatFormatting[] {
                    ChatFormatting.GRAY, ChatFormatting.ITALIC
                });
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

        public void toggleEnabled() {
            Waypoint new_point = point.copyWithChangeEnabled(!point.enabled);
            MinimapApi.getInstance().getMapServer().waypoints.updateWaypoint(point, new_point);
            this.point = new_point;
            this.name = new TextComponent(point.name).withStyle(point.enabled ?
                new ChatFormatting[] {ChatFormatting.WHITE} :
                new ChatFormatting[] {
                    ChatFormatting.GRAY, ChatFormatting.ITALIC
                });
        }

        @Override
        public void render(PoseStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            RenderSystem.setShaderTexture(0, point.getIcon());
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            int abgr = 0xFF000000 | point.colB & 0xFF << 0x10 | point.colG & 0xFF << 0x8 | point.colR & 0xFF;
            AbstractMapRenderer.drawTexCol(poseStack, left + 1, top + 1, height - 2, height - 2, 0, 0, 1, 1, abgr);
            mc.font.draw(
                poseStack,
                Language.getInstance().getVisualOrder(mc.font.substrByWidth(name, width - 100)),
                left + height + 3,
                top + 1,
                0xFFFFFF
            );
            RenderSystem.disableBlend();
        }

    }

}
