package xyz.wagyourtail.minimap.api.config.layers;

import xyz.wagyourtail.minimap.map.image.AbstractImageStrategy;

public abstract class AbstractLayerOptions<T extends AbstractImageStrategy> {
    public abstract T compileLayer();

}
