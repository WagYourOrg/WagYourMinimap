package xyz.wagyourtail.minimap.api.client.config;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.client.gui.hud.map.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.map.RotPlayerArrowOverlay;

@SettingsContainer("gui.wagyourminimap.settings.player_arrow")
public class RotArrowOverlayOptions extends AbstractOverlayOptions<RotPlayerArrowOverlay> {
    @Override
    public RotPlayerArrowOverlay compileOverlay(AbstractMinimapRenderer mapRenderer) {
        return new RotPlayerArrowOverlay(mapRenderer);
    }

}
