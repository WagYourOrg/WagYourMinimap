package xyz.wagyourtail.minimap.api.client.config.layers;

import xyz.wagyourtail.minimap.map.image.ImageStrategy;

public abstract class AbstractLayerOptions<T extends ImageStrategy> {
    public abstract T compileLayer();

}
