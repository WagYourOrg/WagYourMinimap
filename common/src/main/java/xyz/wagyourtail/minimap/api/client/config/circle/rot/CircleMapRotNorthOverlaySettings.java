package xyz.wagyourtail.minimap.api.client.config.circle.rot;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.api.client.config.AbstractOverlayOptions;
import xyz.wagyourtail.minimap.client.gui.hud.map.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.map.circle.rotate.CircleMapRotNorthIcon;

@SettingsContainer("gui.wagyourminimap.settings.overlay.north_icon")
public class CircleMapRotNorthOverlaySettings extends AbstractOverlayOptions<CircleMapRotNorthIcon> {
    @Override
    public CircleMapRotNorthIcon compileOverlay(AbstractMinimapRenderer mapRenderer) {
        return new CircleMapRotNorthIcon(mapRenderer);
    }

}
