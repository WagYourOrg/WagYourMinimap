package xyz.wagyourtail.minimap.api.client.config.overlay;

import xyz.wagyourtail.minimap.client.gui.hud.map.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.overlay.AbstractMinimapOverlay;

public abstract class AbstractOverlaySettings<T extends AbstractMinimapOverlay> {
    public abstract T compileOverlay(AbstractMinimapRenderer mapRenderer);

}
