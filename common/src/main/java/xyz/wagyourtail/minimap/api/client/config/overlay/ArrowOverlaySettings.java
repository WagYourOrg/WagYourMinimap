package xyz.wagyourtail.minimap.api.client.config.overlay;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.client.gui.hud.map.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.overlay.PlayerArrowOverlay;

@SettingsContainer("gui.wagyourminimap.settings.player_arrow")
public class ArrowOverlaySettings extends AbstractOverlaySettings<PlayerArrowOverlay> {
    @Override
    public PlayerArrowOverlay compileOverlay(AbstractMinimapRenderer mapRenderer) {
        return new PlayerArrowOverlay(mapRenderer);
    }

}
