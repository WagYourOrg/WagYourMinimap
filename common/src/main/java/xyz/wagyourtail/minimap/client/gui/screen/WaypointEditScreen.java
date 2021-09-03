package xyz.wagyourtail.minimap.client.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;

public class WaypointEditScreen extends Screen {
    private final Screen parent;

    protected WaypointEditScreen(Screen parent) {
        super(new TranslatableComponent("gui.wagyourminimap.waypoint_edit"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

}
