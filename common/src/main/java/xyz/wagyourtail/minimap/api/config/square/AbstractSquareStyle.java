package xyz.wagyourtail.minimap.api.config.square;

import xyz.wagyourtail.minimap.api.config.AbstractMinimapStyle;
import xyz.wagyourtail.minimap.client.gui.hud.map.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.map.square.SquareMapBorderOverlay;

public abstract class AbstractSquareStyle<T extends AbstractMinimapRenderer> extends AbstractMinimapStyle<T> {

    public AbstractSquareStyle() {
        super();
        availableOverlays.put(SquareMapBorderOverlay.class, SquareMapBorderOverlaySettings.class);
    }

}
