package xyz.wagyourtail.minimap.api.client.config.square.rot;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.api.client.config.AbstractOverlayOptions;
import xyz.wagyourtail.minimap.client.gui.hud.map.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.map.square.rotate.SquareMapRotNorthIcon;

@SettingsContainer("gui.wagyourminimap.settings.overlay.north_icon")
public class SquareMapRotNorthOverlaySettings extends AbstractOverlayOptions<SquareMapRotNorthIcon> {
    @Override
    public SquareMapRotNorthIcon compileOverlay(AbstractMinimapRenderer mapRenderer) {
        return new SquareMapRotNorthIcon(mapRenderer);
    }

}
