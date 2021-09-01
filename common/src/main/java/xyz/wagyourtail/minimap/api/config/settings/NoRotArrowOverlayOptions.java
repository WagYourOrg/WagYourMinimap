package xyz.wagyourtail.minimap.api.config.settings;

import xyz.wagyourtail.minimap.client.gui.renderer.AbstractMapRenderer;
import xyz.wagyourtail.minimap.client.gui.renderer.overlay.NoRotPlayerArrowOverlay;

public class NoRotArrowOverlayOptions extends AbstractOverlayOptions<NoRotPlayerArrowOverlay> {
    @Override
    public NoRotPlayerArrowOverlay compileOverlay(AbstractMapRenderer mapRenderer) {
        return new NoRotPlayerArrowOverlay(mapRenderer);
    }

}
