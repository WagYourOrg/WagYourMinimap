package xyz.wagyourtail.minimap.client.gui;

import xyz.wagyourtail.minimap.client.gui.hud.map.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.overlay.AbstractMinimapOverlay;
import xyz.wagyourtail.minimap.map.image.ImageStrategy;

import java.util.ArrayList;
import java.util.List;

public class MapRendererBuilder<T extends AbstractMinimapRenderer> {

    private final T mapRenderer;
    private final List<ImageStrategy> renderLayers = new ArrayList<>();
    private final List<AbstractMinimapOverlay> overlays = new ArrayList<>();


    private MapRendererBuilder(T mapRenderer) {
        this.mapRenderer = mapRenderer;
    }

    public static <T extends AbstractMinimapRenderer> MapRendererBuilder<T> createBuilder(T mapRenderer) {
        return new MapRendererBuilder<>(mapRenderer);
    }

    public MapRendererBuilder<T> addRenderLayer(ImageStrategy renderLayer) {
        renderLayers.add(renderLayer);
        return this;
    }

    public MapRendererBuilder<T> addOverlay(AbstractMinimapOverlay overlay) {
        overlays.add(overlay);
        return this;
    }

    public T getPartialMapRenderer() {
        return mapRenderer;
    }

    public T build() {
        mapRenderer.setRenderLayers(renderLayers.toArray(ImageStrategy[]::new));
        mapRenderer.setOverlays(overlays.toArray(AbstractMinimapOverlay[]::new));
        return mapRenderer;
    }

}
