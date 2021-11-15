package xyz.wagyourtail.minimap.api.client.config.overlay;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.client.gui.hud.map.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.map.WaypointOverlay;

@SettingsContainer("gui.wagyourminimap.settings.overlay.waypoint")
public class WaypointOverlaySettings extends AbstractOverlaySettings<WaypointOverlay> {
    @Override
    public WaypointOverlay compileOverlay(AbstractMinimapRenderer mapRenderer) {
        return new WaypointOverlay(mapRenderer);
    }

}
