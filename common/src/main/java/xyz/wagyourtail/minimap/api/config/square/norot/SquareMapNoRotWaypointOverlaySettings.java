package xyz.wagyourtail.minimap.api.config.square.norot;

import xyz.wagyourtail.minimap.api.config.AbstractOverlayOptions;
import xyz.wagyourtail.minimap.client.gui.renderer.AbstractMapRenderer;
import xyz.wagyourtail.minimap.client.gui.renderer.square.norot.SquareMapNoRotWaypointOverlay;

public class SquareMapNoRotWaypointOverlaySettings extends AbstractOverlayOptions<SquareMapNoRotWaypointOverlay> {
    @Override
    public SquareMapNoRotWaypointOverlay compileOverlay(AbstractMapRenderer mapRenderer) {
        return new SquareMapNoRotWaypointOverlay(mapRenderer);
    }

}
