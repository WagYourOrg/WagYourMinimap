package xyz.wagyourtail.minimap.client.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;

public class WaypointsScreen extends Screen {
    private final Screen parent;
    protected WaypointsScreen(Screen parent) {
        super(new TranslatableComponent("gui.wagyourminimap.waypoints"));
        this.parent = parent;
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
