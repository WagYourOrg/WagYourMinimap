package xyz.wagyourtail.minimap.client.gui;

import xyz.wagyourtail.minimap.client.gui.image.AbstractImageStrategy;
import xyz.wagyourtail.minimap.client.gui.renderer.AbstractMapRenderer;
import xyz.wagyourtail.minimap.client.gui.renderer.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.client.gui.renderer.overlay.AbstractMapOverlayRenderer;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class MapRendererBuilder<T extends AbstractMinimapRenderer> {

    private final T mapRenderer;
    private final List<AbstractImageStrategy> renderLayers = new ArrayList<>();
    private final List<AbstractMapOverlayRenderer> overlays = new ArrayList<>();


    private MapRendererBuilder(Class<T> mapRenderer) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        this.mapRenderer = mapRenderer.getConstructor().newInstance();
    }

    public static <T extends AbstractMinimapRenderer> MapRendererBuilder<T> createBuilder(Class<T> mapRenderer) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return new MapRendererBuilder<>(mapRenderer);
    }

    public MapRendererBuilder<T> addRenderLayer(Class<? extends AbstractImageStrategy> renderLayer) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        addRenderLayer(renderLayer.getConstructor().newInstance());
        return this;
    }

    public MapRendererBuilder<T> addRenderLayer(AbstractImageStrategy renderLayer) {
        renderLayers.add(renderLayer);
        return this;
    }

    public MapRendererBuilder<T> addOverlay(Class<? extends AbstractMapOverlayRenderer> overlay) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        addOverlay(overlay.getConstructor(AbstractMapRenderer.class).newInstance(mapRenderer));
        return this;
    }

    public MapRendererBuilder<T> addOverlay(AbstractMapOverlayRenderer overlay) {
        overlays.add(overlay);
        return this;
    }

    public T getPartialMapRenderer() {
        return mapRenderer;
    }

    public T build() {
        mapRenderer.setRenderLayers(renderLayers.toArray(AbstractImageStrategy[]::new));
        mapRenderer.setOverlays(overlays.toArray(AbstractMapOverlayRenderer[]::new));
        return mapRenderer;
    }

}
