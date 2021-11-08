package xyz.wagyourtail.minimap.client.gui.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import xyz.wagyourtail.config.gui.MainSettingScreen;
import xyz.wagyourtail.minimap.api.MinimapApi;

public class SettingsScreen extends MainSettingScreen {
    public SettingsScreen(Screen parent) {
        super(new TranslatableComponent("gui.wagyourminimap.settings"), parent, MinimapApi.getInstance().getConfig());
    }

}
