package xyz.wagyourtail.minimap.api.config;

import xyz.wagyourtail.minimap.client.gui.renderer.AbstractMapRenderer;
import xyz.wagyourtail.minimap.client.gui.renderer.overlay.AbstractMapOverlayRenderer;

public abstract class AbstractOverlayOptions<T extends AbstractMapOverlayRenderer> {
    public abstract T compileOverlay(AbstractMapRenderer mapRenderer);
}
