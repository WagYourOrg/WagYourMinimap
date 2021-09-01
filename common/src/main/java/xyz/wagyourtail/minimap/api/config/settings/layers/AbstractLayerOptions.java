package xyz.wagyourtail.minimap.api.config.settings.layers;

import xyz.wagyourtail.minimap.client.gui.image.AbstractImageStrategy;

public abstract class AbstractLayerOptions<T extends AbstractImageStrategy> {
    public abstract T compileLayer();
}
