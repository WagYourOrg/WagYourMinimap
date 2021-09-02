package xyz.wagyourtail.minimap.api.config.square;

import xyz.wagyourtail.minimap.api.config.AbstractOverlayOptions;
import xyz.wagyourtail.minimap.client.gui.renderer.AbstractMapRenderer;
import xyz.wagyourtail.minimap.client.gui.renderer.square.SquareMapBorderOverlay;

public class SquareMapBorderOverlaySettings extends AbstractOverlayOptions<SquareMapBorderOverlay> {
    @Override
    public SquareMapBorderOverlay compileOverlay(AbstractMapRenderer mapRenderer) {
        return new SquareMapBorderOverlay(mapRenderer);
    }

}
