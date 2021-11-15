package xyz.wagyourtail.minimap.api.client.config.overlay.fullscreen;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.client.gui.screen.map.PlayerIconOverlay;

@SettingsContainer("gui.wagyourminimap.settings.overlay.player_icons")
public class PlayerIconSettings extends AbstractFullscreenOverlaySettings<PlayerIconOverlay> {
    @Override
    public PlayerIconOverlay compileOverlay() {
        return new PlayerIconOverlay(MinimapClientApi.getInstance().screen.renderer);
    }

}
