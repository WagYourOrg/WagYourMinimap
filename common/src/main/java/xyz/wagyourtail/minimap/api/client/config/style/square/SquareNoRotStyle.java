package xyz.wagyourtail.minimap.api.client.config.style.square;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.api.client.config.overlay.AbstractOverlaySettings;
import xyz.wagyourtail.minimap.api.client.config.overlay.ArrowOverlaySettings;
import xyz.wagyourtail.minimap.api.client.config.overlay.SquareMapBorderOverlaySettings;
import xyz.wagyourtail.minimap.api.client.config.overlay.WaypointOverlaySettings;
import xyz.wagyourtail.minimap.client.gui.hud.map.SquareMapRenderer;

@SettingsContainer("gui.wagyourminimap.settings.square_norot_style")
public class SquareNoRotStyle extends AbstractSquareStyle {
    public SquareNoRotStyle() {
        super();

        //default overlays
        overlays = new AbstractOverlaySettings[] {
            new SquareMapBorderOverlaySettings(),
            new ArrowOverlaySettings(),
            new WaypointOverlaySettings()
        };
    }

    @Override
    protected SquareMapRenderer getMapRenderer() {
        return new SquareMapRenderer(false);
    }

}
