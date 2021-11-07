package xyz.wagyourtail.minimap.api.config.square.rot;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.api.config.AbstractOverlayOptions;
import xyz.wagyourtail.minimap.api.config.RotArrowOverlayOptions;
import xyz.wagyourtail.minimap.api.config.square.AbstractSquareStyle;
import xyz.wagyourtail.minimap.api.config.square.SquareMapBorderOverlaySettings;
import xyz.wagyourtail.minimap.client.gui.renderer.overlay.RotPlayerArrowOverlay;
import xyz.wagyourtail.minimap.client.gui.renderer.square.rotate.SquareMapRotRenderer;

@SettingsContainer("gui.wagyourminimap.settings.square_rot_style")
public class SquareRotStyle extends AbstractSquareStyle<SquareMapRotRenderer> {

    public SquareRotStyle() {
        super();
        availableOverlays.put(RotPlayerArrowOverlay.class, RotArrowOverlayOptions.class);
//        availableOverlays.put(SquareMapNoRotWaypointOverlay.class, SquareMapNoRotWaypointOverlaySettings.class);

        //default overlays
        overlays = new AbstractOverlayOptions[] {new SquareMapBorderOverlaySettings(), new RotArrowOverlayOptions()};
    }

    @Override
    protected Class<SquareMapRotRenderer> getMapRenderer() {
        return SquareMapRotRenderer.class;
    }


}
