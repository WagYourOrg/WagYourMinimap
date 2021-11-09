package xyz.wagyourtail.minimap.api.client.config.circle.rot;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.api.client.config.AbstractOverlayOptions;
import xyz.wagyourtail.minimap.client.gui.hud.map.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.map.circle.rotate.CircleMapRotWaypointOverlay;

@SettingsContainer("gui.wagyourminimap.settings.overlay.waypoint")
public class CircleMapRotWaypointOverlaySettings extends AbstractOverlayOptions<CircleMapRotWaypointOverlay> {
    @Override
    public CircleMapRotWaypointOverlay compileOverlay(AbstractMinimapRenderer mapRenderer) {
        return new CircleMapRotWaypointOverlay(mapRenderer);
    }
}
