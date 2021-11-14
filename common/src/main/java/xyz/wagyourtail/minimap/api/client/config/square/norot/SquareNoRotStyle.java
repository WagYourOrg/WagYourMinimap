package xyz.wagyourtail.minimap.api.client.config.square.norot;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.api.client.config.AbstractOverlayOptions;
import xyz.wagyourtail.minimap.api.client.config.NoRotArrowOverlayOptions;
import xyz.wagyourtail.minimap.api.client.config.square.AbstractSquareStyle;
import xyz.wagyourtail.minimap.api.client.config.square.SquareMapBorderOverlaySettings;
import xyz.wagyourtail.minimap.client.gui.hud.map.NoRotPlayerArrowOverlay;
import xyz.wagyourtail.minimap.client.gui.hud.map.square.norot.SquareMapNoRotRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.map.square.norot.SquareMapNoRotWaypointOverlay;

@SettingsContainer("gui.wagyourminimap.settings.square_norot_style")
public class SquareNoRotStyle extends AbstractSquareStyle<SquareMapNoRotRenderer> {
    public SquareNoRotStyle() {
        super();
        availableOverlays.put(NoRotPlayerArrowOverlay.class, NoRotArrowOverlayOptions.class);
        availableOverlays.put(SquareMapNoRotWaypointOverlay.class, SquareMapNoRotWaypointOverlaySettings.class);

        //default overlays
        overlays = new AbstractOverlayOptions[] {
            new SquareMapBorderOverlaySettings(),
            new NoRotArrowOverlayOptions(),
            new SquareMapNoRotWaypointOverlaySettings()
        };
    }

    @Override
    protected Class<SquareMapNoRotRenderer> getMapRenderer() {
        return SquareMapNoRotRenderer.class;
    }

}
