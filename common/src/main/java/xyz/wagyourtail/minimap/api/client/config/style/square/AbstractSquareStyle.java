package xyz.wagyourtail.minimap.api.client.config.style.square;

import xyz.wagyourtail.minimap.api.client.config.overlay.SquareMapBorderOverlaySettings;
import xyz.wagyourtail.minimap.api.client.config.style.AbstractMinimapStyle;
import xyz.wagyourtail.minimap.client.gui.hud.map.SquareMapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.overlay.SquareMapBorderOverlay;

public abstract class AbstractSquareStyle extends AbstractMinimapStyle<SquareMapRenderer> {

    protected AbstractSquareStyle(boolean rotate) {
        super(rotate);
        availableOverlays.put(SquareMapBorderOverlay.class, SquareMapBorderOverlaySettings.class);
    }

}
