package xyz.wagyourtail.minimap.api.client.config.overlay;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.client.gui.hud.map.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.overlay.SquareMapBorderOverlay;

@SettingsContainer("gui.wagyourminimap.settings.overlay.map_border")
public class SquareMapBorderOverlaySettings extends AbstractOverlaySettings<SquareMapBorderOverlay> {
    @Override
    public SquareMapBorderOverlay compileOverlay(AbstractMinimapRenderer mapRenderer) {
        return new SquareMapBorderOverlay(mapRenderer);
    }

}
