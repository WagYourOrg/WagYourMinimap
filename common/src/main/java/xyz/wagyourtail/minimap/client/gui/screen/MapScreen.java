package xyz.wagyourtail.minimap.client.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.phys.Vec3;

public class MapScreen extends Screen {
    Vec3 screen_center;

    public MapScreen() {
        super(new TranslatableComponent("gui.wagyourminimap.title"));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean consumed = super.mouseClicked(mouseX, mouseY, button);
        if (!consumed && button == 1) {
            //on right click map
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.render(poseStack, mouseX, mouseY, partialTicks);

    }

}
