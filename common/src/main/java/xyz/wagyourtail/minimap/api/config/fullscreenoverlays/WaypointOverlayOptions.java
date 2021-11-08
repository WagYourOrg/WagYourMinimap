package xyz.wagyourtail.minimap.api.config.fullscreenoverlays;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.client.gui.screen.map.WaypointOverlay;

@SettingsContainer("gui.wagyourminimap.settings.overlay.waypoint")
public class WaypointOverlayOptions extends AbstractFullscreenOverlayOptions<WaypointOverlay> {
    @Override
    public WaypointOverlay compileOverlay() {
        return new WaypointOverlay(MinimapClientApi.getInstance().screen.renderer);
    }

}
