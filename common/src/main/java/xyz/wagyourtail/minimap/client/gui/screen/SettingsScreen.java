package xyz.wagyourtail.minimap.client.gui.screen;

import net.minecraft.client.gui.screens.Screen;
import xyz.wagyourtail.config.gui.MainSettingScreen;
import xyz.wagyourtail.minimap.api.MinimapApi;

public class SettingsScreen extends MainSettingScreen {
    public SettingsScreen(Screen parent) {
        super(net.minecraft.network.chat.Component.translatable("gui.wagyourminimap.settings"), parent, MinimapApi.getInstance().getConfig());
    }

}
