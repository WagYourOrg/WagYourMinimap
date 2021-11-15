package xyz.wagyourtail.minimap.api.client.config.overlay.fullscreen;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.client.gui.screen.map.DataOverlay;

@SettingsContainer("gui.wagyourminimap.settings.overlay.data")
public class DataOverlaySettings extends AbstractFullscreenOverlaySettings<DataOverlay> {
    @Override
    public DataOverlay compileOverlay() {
        return new DataOverlay(MinimapClientApi.getInstance().screen.renderer);
    }

}
