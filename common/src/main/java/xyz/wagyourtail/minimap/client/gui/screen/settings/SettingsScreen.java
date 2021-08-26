package xyz.wagyourtail.minimap.client.gui.screen.settings;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;

public class SettingsScreen extends Screen {
    private final Screen parent;

    public SettingsScreen(Screen parent) {
        super(new TranslatableComponent("gui.wagyourminimap.settings"));
        this.parent = parent;
    }

    @Override
    public void onClose() {
        assert minecraft != null;
        minecraft.setScreen(this.parent);
    }
}
