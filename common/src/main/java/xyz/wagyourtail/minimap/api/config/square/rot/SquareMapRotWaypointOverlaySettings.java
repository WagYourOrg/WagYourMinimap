package xyz.wagyourtail.minimap.api.config.square.rot;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.api.config.AbstractOverlayOptions;
import xyz.wagyourtail.minimap.client.gui.renderer.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.client.gui.renderer.square.rotate.SquareMapRotWaypointOverlay;

@SettingsContainer("gui.wagyourminimap.settings.overlay.waypoint")
public class SquareMapRotWaypointOverlaySettings extends AbstractOverlayOptions<SquareMapRotWaypointOverlay> {
    @Override
    public SquareMapRotWaypointOverlay compileOverlay(AbstractMinimapRenderer mapRenderer) {
        return new SquareMapRotWaypointOverlay(mapRenderer);
    }
}
