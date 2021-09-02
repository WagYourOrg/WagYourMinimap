package xyz.wagyourtail.minimap.client.gui.screen.settings;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.wagyourconfig.gui.MainSettingScreen;

public class SettingsScreen extends MainSettingScreen {
    public SettingsScreen(Screen parent) {
        super(new TranslatableComponent("gui.wagyourminimap.settings"), parent, MinimapApi.getInstance().getConfig());
    }
}
