package xyz.wagyourtail.minimap.api.client.config.overlay;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.client.gui.hud.map.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.overlay.CircleMapBorderOverlay;


@SettingsContainer("gui.wagyourminimap.settings.overlay.map_border")
public class CircleMapBorderOverlaySettings extends AbstractOverlaySettings<CircleMapBorderOverlay> {
    @Override
    public CircleMapBorderOverlay compileOverlay(AbstractMinimapRenderer mapRenderer) {
        return new CircleMapBorderOverlay(mapRenderer);
    }

}
