package xyz.wagyourtail.minimap.api.config.settings;

import xyz.wagyourtail.minimap.api.config.SettingsContainer;
import xyz.wagyourtail.minimap.client.gui.renderer.AbstractMapRenderer;
import xyz.wagyourtail.minimap.client.gui.renderer.overlay.AbstractMapOverlayRenderer;

@SettingsContainer
public abstract class AbstractOverlayOptions<T extends AbstractMapOverlayRenderer> {
    public abstract T compileOverlay(AbstractMapRenderer mapRenderer);
}
