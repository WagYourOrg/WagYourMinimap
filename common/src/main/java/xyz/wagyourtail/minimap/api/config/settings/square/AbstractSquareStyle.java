package xyz.wagyourtail.minimap.api.config.settings.square;

import xyz.wagyourtail.minimap.api.config.settings.AbstractMinimapStyle;
import xyz.wagyourtail.minimap.client.gui.renderer.AbstractMapRenderer;
import xyz.wagyourtail.minimap.client.gui.renderer.square.SquareMapBorderOverlay;

public abstract class AbstractSquareStyle<T extends AbstractMapRenderer> extends AbstractMinimapStyle<T> {

    public AbstractSquareStyle() {
        super();
        availableOverlays.put(SquareMapBorderOverlay.class, SquareMapBorderOverlaySettings.class);
    }
}
