package xyz.wagyourtail.minimap.api.config.circle.rot;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.api.config.AbstractOverlayOptions;
import xyz.wagyourtail.minimap.client.gui.renderer.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.client.gui.renderer.circle.rot.CircleMapRotNorthIcon;
import xyz.wagyourtail.minimap.client.gui.renderer.square.rotate.SquareMapRotNorthIcon;

@SettingsContainer("gui.wagyourminimap.settings.overlay.north_icon")
public class CircleMapRotNorthOverlaySettings extends AbstractOverlayOptions<CircleMapRotNorthIcon> {
    @Override
    public CircleMapRotNorthIcon compileOverlay(AbstractMinimapRenderer mapRenderer) {
        return new CircleMapRotNorthIcon(mapRenderer);
    }

}
