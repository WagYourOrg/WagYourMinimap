package xyz.wagyourtail.minimap.api.config.square.norot;

import xyz.wagyourtail.minimap.api.config.AbstractOverlayOptions;
import xyz.wagyourtail.minimap.api.config.NoRotArrowOverlayOptions;
import xyz.wagyourtail.minimap.api.config.square.AbstractSquareStyle;
import xyz.wagyourtail.minimap.api.config.square.SquareMapBorderOverlaySettings;
import xyz.wagyourtail.minimap.client.gui.renderer.overlay.NoRotPlayerArrowOverlay;
import xyz.wagyourtail.minimap.client.gui.renderer.square.norot.SquareMapNoRotRenderer;
import xyz.wagyourtail.minimap.client.gui.renderer.square.norot.SquareMapNoRotWaypointOverlay;
import xyz.wagyourtail.wagyourconfig.field.SettingsContainer;

@SettingsContainer("gui.wagyourminimap.settings.square_norot_style")
public class SquareNoRotStyle extends AbstractSquareStyle<SquareMapNoRotRenderer> {
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
