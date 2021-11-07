package xyz.wagyourtail.minimap.api.config;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.client.gui.renderer.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.client.gui.renderer.overlay.RotPlayerArrowOverlay;

@SettingsContainer("gui.wagyourminimap.settings.player_arrow")
public class RotArrowOverlayOptions  extends AbstractOverlayOptions<RotPlayerArrowOverlay> {
    @Override
    public RotPlayerArrowOverlay compileOverlay(AbstractMinimapRenderer mapRenderer) {
        return new RotPlayerArrowOverlay(mapRenderer);
    }
}
