package xyz.wagyourtail.minimap.api.client.config.overlay.rotate;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.api.client.config.overlay.AbstractOverlaySettings;
import xyz.wagyourtail.minimap.client.gui.hud.map.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.overlay.rotate.NorthIconOverlay;

@SettingsContainer("gui.wagyourminimap.settings.overlay.north_icon")
public class NorthIconOverlaySettings extends AbstractOverlaySettings<NorthIconOverlay> {
    @Override
    public NorthIconOverlay compileOverlay(AbstractMinimapRenderer mapRenderer) {
        return new NorthIconOverlay(mapRenderer);
    }

}
