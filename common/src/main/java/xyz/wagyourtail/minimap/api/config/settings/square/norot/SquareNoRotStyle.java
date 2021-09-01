package xyz.wagyourtail.minimap.api.config.settings.square.norot;

import xyz.wagyourtail.minimap.api.config.settings.AbstractMinimapStyle;
import xyz.wagyourtail.minimap.api.config.settings.AbstractOverlayOptions;
import xyz.wagyourtail.minimap.api.config.settings.square.SquareMapBorderOverlaySettings;
import xyz.wagyourtail.minimap.api.config.settings.NoRotArrowOverlayOptions;
import xyz.wagyourtail.minimap.client.gui.renderer.overlay.NoRotPlayerArrowOverlay;
import xyz.wagyourtail.minimap.client.gui.renderer.square.norot.SquareMapNoRotRenderer;
import xyz.wagyourtail.minimap.client.gui.renderer.square.norot.SquareMapNoRotWaypointOverlay;

public class SquareNoRotStyle extends AbstractMinimapStyle<SquareMapNoRotRenderer> {
    public SquareNoRotStyle() {
        super();
        availableOverlays.put(NoRotPlayerArrowOverlay.class, NoRotArrowOverlayOptions.class);
        availableOverlays.put(SquareMapNoRotWaypointOverlay.class, SquareMapNoRotWaypointOverlaySettings.class);

        //default overlays
        overlays = new AbstractOverlayOptions[] {new SquareMapBorderOverlaySettings(), new NoRotArrowOverlayOptions(), new SquareMapNoRotWaypointOverlaySettings()};
    }

    @Override
    protected Class<SquareMapNoRotRenderer> getMapRenderer() {
        return SquareMapNoRotRenderer.class;
    }

}
