package xyz.wagyourtail.minimap.api.config.square.norot;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.api.config.AbstractOverlayOptions;
import xyz.wagyourtail.minimap.client.gui.renderer.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.client.gui.renderer.square.norot.SquareMapNoRotWaypointOverlay;

@SettingsContainer("gui.wagyourminimap.settings.overlay.waypoint")
public class SquareMapNoRotWaypointOverlaySettings extends AbstractOverlayOptions<SquareMapNoRotWaypointOverlay> {
    @Override
    public SquareMapNoRotWaypointOverlay compileOverlay(AbstractMinimapRenderer mapRenderer) {
        return new SquareMapNoRotWaypointOverlay(mapRenderer);
    }

}
