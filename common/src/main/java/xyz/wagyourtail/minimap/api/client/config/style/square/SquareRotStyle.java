package xyz.wagyourtail.minimap.api.client.config.style.square;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.api.client.config.overlay.*;
import xyz.wagyourtail.minimap.api.client.config.overlay.rotate.NorthIconOverlaySettings;
import xyz.wagyourtail.minimap.client.gui.hud.map.SquareMapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.overlay.rotate.NorthIconOverlay;

@SettingsContainer("gui.wagyourminimap.settings.square_rot_style")
public class SquareRotStyle extends AbstractSquareStyle {
    public SquareRotStyle() {
        super(true);
        availableOverlays.put(NorthIconOverlay.class, NorthIconOverlaySettings.class);

        //default overlays
        overlays = new AbstractOverlaySettings[] {
            new SquareMapBorderOverlaySettings(),
            new ArrowOverlaySettings(),
            new WaypointOverlaySettings(),
            new NorthIconOverlaySettings(),
            new MobIconOverlaySettings()
        };
    }

    @Override
    protected SquareMapRenderer getMapRenderer() {
        return new SquareMapRenderer(rotate);
    }

}
