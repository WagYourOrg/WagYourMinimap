package xyz.wagyourtail.minimap.api.client.config.fullscreenoverlays;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.client.gui.screen.map.ScaleOverlay;

@SettingsContainer("gui.wagyourminimap.settings.overlay.scale")
public class ScaleOverlayOptions extends AbstractFullscreenOverlayOptions<ScaleOverlay> {
    @Override
    public ScaleOverlay compileOverlay() {
        return new ScaleOverlay(MinimapClientApi.getInstance().screen.renderer);
    }

}
