package xyz.wagyourtail.minimap.api.config.circle;

import xyz.wagyourtail.minimap.api.config.AbstractMinimapStyle;
import xyz.wagyourtail.minimap.api.config.square.SquareMapBorderOverlaySettings;
import xyz.wagyourtail.minimap.client.gui.renderer.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.client.gui.renderer.circle.CircleMapBorderOverlay;
import xyz.wagyourtail.minimap.client.gui.renderer.square.SquareMapBorderOverlay;

public abstract class AbstractCircleStyle<T extends AbstractMinimapRenderer> extends AbstractMinimapStyle<T> {

    public AbstractCircleStyle() {
        super();
        availableOverlays.put(CircleMapBorderOverlay.class, SquareMapBorderOverlaySettings.class);
    }
}
