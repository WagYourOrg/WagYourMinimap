package xyz.wagyourtail.minimap.api.config.circle.rot;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.api.config.AbstractOverlayOptions;
import xyz.wagyourtail.minimap.client.gui.renderer.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.client.gui.renderer.circle.rot.CircleMapRotWaypointOverlay;
import xyz.wagyourtail.minimap.client.gui.renderer.square.rotate.SquareMapRotWaypointOverlay;

@SettingsContainer("gui.wagyourminimap.settings.overlay.waypoint")
public class CircleMapRotWaypointOverlaySettings extends AbstractOverlayOptions<CircleMapRotWaypointOverlay> {
    @Override
    public CircleMapRotWaypointOverlay compileOverlay(AbstractMinimapRenderer mapRenderer) {
        return new CircleMapRotWaypointOverlay(mapRenderer);
    }
}
