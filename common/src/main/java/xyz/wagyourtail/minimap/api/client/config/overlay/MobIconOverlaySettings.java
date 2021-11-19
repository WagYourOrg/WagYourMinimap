package xyz.wagyourtail.minimap.api.client.config.overlay;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.client.gui.hud.map.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.overlay.MobIconOverlay;

@SettingsContainer("gui.wagyourminimap.settings.overlays.mob_icon")
public class MobIconOverlaySettings extends AbstractOverlaySettings<MobIconOverlay> {
    @Override
    public MobIconOverlay compileOverlay(AbstractMinimapRenderer mapRenderer) {
        return new MobIconOverlay(mapRenderer);
    }

}
