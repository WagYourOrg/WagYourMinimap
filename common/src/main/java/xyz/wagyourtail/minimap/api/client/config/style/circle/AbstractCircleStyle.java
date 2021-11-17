package xyz.wagyourtail.minimap.api.client.config.style.circle;

import xyz.wagyourtail.minimap.api.client.config.overlay.SquareMapBorderOverlaySettings;
import xyz.wagyourtail.minimap.api.client.config.style.AbstractMinimapStyle;
import xyz.wagyourtail.minimap.client.gui.hud.map.CircleMapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.overlay.CircleMapBorderOverlay;

public abstract class AbstractCircleStyle extends AbstractMinimapStyle<CircleMapRenderer> {

    protected AbstractCircleStyle(boolean rotate) {
        super(rotate);
        availableOverlays.put(CircleMapBorderOverlay.class, SquareMapBorderOverlaySettings.class);
    }

}
