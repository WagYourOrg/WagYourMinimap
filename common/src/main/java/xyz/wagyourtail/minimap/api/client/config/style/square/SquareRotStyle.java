package xyz.wagyourtail.minimap.api.client.config.style.square;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.api.client.config.overlay.AbstractOverlaySettings;
import xyz.wagyourtail.minimap.api.client.config.overlay.ArrowOverlaySettings;
import xyz.wagyourtail.minimap.api.client.config.overlay.SquareMapBorderOverlaySettings;
import xyz.wagyourtail.minimap.api.client.config.overlay.WaypointOverlaySettings;
import xyz.wagyourtail.minimap.api.client.config.overlay.rotate.NorthIconOverlaySettings;
import xyz.wagyourtail.minimap.client.gui.hud.map.SquareMapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.overlay.rotate.NorthIconOverlay;

@SettingsContainer("gui.wagyourminimap.settings.square_norot_style")
public class SquareRotStyle extends AbstractSquareStyle {
    public SquareRotStyle() {
        super();
        availableOverlays.put(NorthIconOverlay.class, NorthIconOverlaySettings.class);

        //default overlays
        overlays = new AbstractOverlaySettings[] {
            new SquareMapBorderOverlaySettings(),
            new ArrowOverlaySettings(),
            new WaypointOverlaySettings(),
            new NorthIconOverlaySettings()
        };
    }

    @Override
    protected SquareMapRenderer getMapRenderer() {
        return new SquareMapRenderer(true);
    }

}
