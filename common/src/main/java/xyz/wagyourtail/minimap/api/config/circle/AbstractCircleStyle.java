package xyz.wagyourtail.minimap.api.config.circle;

import xyz.wagyourtail.minimap.api.config.AbstractMinimapStyle;
import xyz.wagyourtail.minimap.api.config.square.SquareMapBorderOverlaySettings;
import xyz.wagyourtail.minimap.client.gui.hud.map.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.map.circle.CircleMapBorderOverlay;

public abstract class AbstractCircleStyle<T extends AbstractMinimapRenderer> extends AbstractMinimapStyle<T> {

    public AbstractCircleStyle() {
        super();
        availableOverlays.put(CircleMapBorderOverlay.class, SquareMapBorderOverlaySettings.class);
    }
}
