package xyz.wagyourtail.minimap.api.client.config.circle.norot;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.api.client.config.AbstractOverlayOptions;
import xyz.wagyourtail.minimap.client.gui.hud.map.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.map.circle.norot.CircleMapNoRotWaypointOverlay;

@SettingsContainer("gui.wagyourminimap.settings.overlay.waypoint")
public class CircleMapNoRotWaypointOverlaySettings extends AbstractOverlayOptions<CircleMapNoRotWaypointOverlay> {
    @Override
    public CircleMapNoRotWaypointOverlay compileOverlay(AbstractMinimapRenderer mapRenderer) {
        return new CircleMapNoRotWaypointOverlay(mapRenderer);
    }
}
