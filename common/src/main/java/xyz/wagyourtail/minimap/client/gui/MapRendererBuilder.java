package xyz.wagyourtail.minimap.client.gui;

import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.client.gui.image.AbstractImageStrategy;
import xyz.wagyourtail.minimap.client.gui.renderer.AbstractMapRenderer;
import xyz.wagyourtail.minimap.client.gui.renderer.overlay.AbstractMapOverlayRenderer;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class MapRendererBuilder<T extends AbstractMapRenderer> {

    private final T mapRenderer;
    private final List<AbstractImageStrategy> renderLayers = new ArrayList<>();
    private final List<AbstractMapOverlayRenderer> overlays = new ArrayList<>();


    private MapRendererBuilder(Class<T> mapRenderer) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        this.mapRenderer = mapRenderer.getConstructor(AbstractMapGui.class).newInstance(MinimapClientApi.inGameHud);
    }

    public static <T extends AbstractMapRenderer> MapRendererBuilder<T> createBuilder(Class<T> mapRenderer) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return new MapRendererBuilder<>(mapRenderer);
    }

    public MapRendererBuilder<T> addRenderLayer(Class<? extends AbstractImageStrategy> renderLayer) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        renderLayers.add(renderLayer.getConstructor().newInstance());
        return this;
    }

    public MapRendererBuilder<T> addOverlay(Class<? extends AbstractMapOverlayRenderer> overlay) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        overlays.add(overlay.getConstructor(AbstractMapRenderer.class).newInstance(mapRenderer));
        return this;
    }

    public T build() {
        mapRenderer.setRenderLayers(renderLayers.toArray(AbstractImageStrategy[]::new));
        mapRenderer.setOverlays(overlays.toArray(AbstractMapOverlayRenderer[]::new));
        return mapRenderer;
    }

}
