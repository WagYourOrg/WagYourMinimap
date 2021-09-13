package xyz.wagyourtail.minimap.api.config.fullscreenoverlays;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.client.gui.screen.renderer.overlay.PlayerIconOverlay;

@SettingsContainer("gui.wagyourminimap.settings.overlay.player_icons")
public class PlayerIconOptions extends AbstractFullscreenOverlayOptions<PlayerIconOverlay> {
    @Override
    public PlayerIconOverlay compileOverlay() {
        return new PlayerIconOverlay(MinimapClientApi.getInstance().screen.renderer);
    }

}
