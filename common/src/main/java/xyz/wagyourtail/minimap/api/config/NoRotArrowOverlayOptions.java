package xyz.wagyourtail.minimap.api.config;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.client.gui.hud.map.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.map.NoRotPlayerArrowOverlay;

@SettingsContainer("gui.wagyourminimap.settings.player_arrow")
public class NoRotArrowOverlayOptions extends AbstractOverlayOptions<NoRotPlayerArrowOverlay> {
    @Override
    public NoRotPlayerArrowOverlay compileOverlay(AbstractMinimapRenderer mapRenderer) {
        return new NoRotPlayerArrowOverlay(mapRenderer);
    }

}
