package xyz.wagyourtail.minimap.api.config.square;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.api.config.AbstractOverlayOptions;
import xyz.wagyourtail.minimap.client.gui.hud.map.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.map.square.SquareMapBorderOverlay;

@SettingsContainer("gui.wagyourminimap.settings.overlay.map_border")
public class SquareMapBorderOverlaySettings extends AbstractOverlayOptions<SquareMapBorderOverlay> {
    @Override
    public SquareMapBorderOverlay compileOverlay(AbstractMinimapRenderer mapRenderer) {
        return new SquareMapBorderOverlay(mapRenderer);
    }

}
