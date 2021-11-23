package xyz.wagyourtail.minimap.api.client.config.style.square;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.api.client.config.overlay.*;
import xyz.wagyourtail.minimap.client.gui.hud.map.SquareMapRenderer;

@SettingsContainer("gui.wagyourminimap.settings.square_norot_style")
public class SquareNoRotStyle extends AbstractSquareStyle {
    public SquareNoRotStyle() {
        super(false);

        //default overlays
        overlays = new AbstractOverlaySettings[] {
            new SquareMapBorderOverlaySettings(),
            new ArrowOverlaySettings(),
            new WaypointOverlaySettings(),
            new MobIconOverlaySettings()
        };
    }

    @Override
    protected SquareMapRenderer getMapRenderer() {
        return new SquareMapRenderer(rotate);
    }

}
