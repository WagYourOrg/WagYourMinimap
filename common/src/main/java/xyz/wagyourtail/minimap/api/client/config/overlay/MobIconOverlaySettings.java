package xyz.wagyourtail.minimap.api.client.config.overlay;

import xyz.wagyourtail.config.field.DoubleRange;
import xyz.wagyourtail.config.field.IntRange;
import xyz.wagyourtail.config.field.Setting;
import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.client.gui.hud.map.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.overlay.MobIconOverlay;

@SettingsContainer("gui.wagyourminimap.settings.overlays.mob_icon")
public class MobIconOverlaySettings extends AbstractOverlaySettings<MobIconOverlay> {

    @Setting(value = "gui.wagyourminimap.settings.overlays.mob_icon.max_scale")
    @DoubleRange(from = 0.0, to = 1.0, steps = 100)
    public double maxScale = .07;

    @Setting(value = "gui.wagyourminimap.settings.overlays.mob_icon.max_size")
    @IntRange(from = 0, to = 100)
    public int maxSize = 10;

    @Override
    public MobIconOverlay compileOverlay(AbstractMinimapRenderer mapRenderer) {
        return new MobIconOverlay(mapRenderer, (float) maxScale, maxSize);
    }

}
