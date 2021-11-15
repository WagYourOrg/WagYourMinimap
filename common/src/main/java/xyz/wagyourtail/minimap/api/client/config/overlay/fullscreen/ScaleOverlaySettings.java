package xyz.wagyourtail.minimap.api.client.config.overlay.fullscreen;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.client.gui.screen.map.ScaleOverlay;

@SettingsContainer("gui.wagyourminimap.settings.overlay.scale")
public class ScaleOverlaySettings extends AbstractFullscreenOverlaySettings<ScaleOverlay> {
    @Override
    public ScaleOverlay compileOverlay() {
        return new ScaleOverlay(MinimapClientApi.getInstance().screen.renderer);
    }

}
