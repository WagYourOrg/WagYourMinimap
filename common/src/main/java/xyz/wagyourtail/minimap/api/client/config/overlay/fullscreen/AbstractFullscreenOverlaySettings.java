package xyz.wagyourtail.minimap.api.client.config.overlay.fullscreen;

import xyz.wagyourtail.minimap.client.gui.screen.map.AbstractFullscreenOverlay;

public abstract class AbstractFullscreenOverlaySettings<T extends AbstractFullscreenOverlay> {

    public abstract T compileOverlay();

}
