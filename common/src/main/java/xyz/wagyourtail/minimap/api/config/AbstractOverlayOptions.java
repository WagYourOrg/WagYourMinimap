package xyz.wagyourtail.minimap.api.config;

import xyz.wagyourtail.minimap.client.gui.hud.map.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.map.AbstractMapOverlayRenderer;

public abstract class AbstractOverlayOptions<T extends AbstractMapOverlayRenderer> {
    public abstract T compileOverlay(AbstractMinimapRenderer mapRenderer);

}
