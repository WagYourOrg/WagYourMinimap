package xyz.wagyourtail.minimap.api.config;

import xyz.wagyourtail.minimap.client.gui.renderer.AbstractMapRenderer;
import xyz.wagyourtail.minimap.client.gui.renderer.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.client.gui.renderer.overlay.NoRotPlayerArrowOverlay;
import xyz.wagyourtail.wagyourconfig.field.SettingsContainer;

@SettingsContainer("gui.wagyourminimap.settings.player_arrow")
public class NoRotArrowOverlayOptions extends AbstractOverlayOptions<NoRotPlayerArrowOverlay> {
    @Override
    public NoRotPlayerArrowOverlay compileOverlay(AbstractMinimapRenderer mapRenderer) {
        return new NoRotPlayerArrowOverlay(mapRenderer);
    }

}
