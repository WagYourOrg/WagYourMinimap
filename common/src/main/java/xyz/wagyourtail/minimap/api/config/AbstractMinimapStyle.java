package xyz.wagyourtail.minimap.api.config;

import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.wagyourconfig.field.Setting;
import xyz.wagyourtail.minimap.api.config.layers.AbstractLayerOptions;
import xyz.wagyourtail.minimap.api.config.layers.LightLayer;
import xyz.wagyourtail.minimap.api.config.layers.VanillaMapLayer;
import xyz.wagyourtail.minimap.client.gui.MapRendererBuilder;
import xyz.wagyourtail.minimap.client.gui.image.AbstractImageStrategy;
import xyz.wagyourtail.minimap.client.gui.image.BlockLightImageStrategy;
import xyz.wagyourtail.minimap.client.gui.image.VanillaMapImageStrategy;
import xyz.wagyourtail.minimap.client.gui.renderer.AbstractMapRenderer;
import xyz.wagyourtail.minimap.client.gui.renderer.overlay.AbstractMapOverlayRenderer;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


@SuppressWarnings("rawtypes")
public abstract class AbstractMinimapStyle<T extends AbstractMapRenderer> {
    public Map<Class<? extends AbstractMapOverlayRenderer>, Class<? extends AbstractOverlayOptions>> availableOverlays = new HashMap<>();

    public Map<Class<? extends AbstractImageStrategy>, Class<? extends AbstractLayerOptions>> availableLayers = new HashMap<>();

    @Setting(value = "gui.wagyourminimap.settings.style.overlay", options = "overlayOptions", setter = "setOverlays")
    public AbstractOverlayOptions<?>[] overlays;

    @Setting(value = "gui.wagyourminimap.settings.style.layers", options = "layerOptions", setter = "setLayers")
    public AbstractLayerOptions<?>[] layers;

    public AbstractMinimapStyle() {
        // default layers
        layers = new AbstractLayerOptions[] {new VanillaMapLayer(), new LightLayer()};

        //layer register
        availableLayers.put(VanillaMapImageStrategy.class, VanillaMapLayer.class);
        availableLayers.put(BlockLightImageStrategy.class, LightLayer.class);
    }

    public Collection<Class<? extends AbstractOverlayOptions>> overlayOptions() {
        return availableOverlays.values();
    }

    public Collection<Class<? extends AbstractLayerOptions>> layerOptions() {
        return availableLayers.values();
    }

    public void setOverlays(AbstractOverlayOptions<?>[] overlays) {
        this.overlays = overlays;
        AbstractMapRenderer renderer = MinimapClientApi.getInstance().inGameHud.getRenderer();
        renderer.setOverlays(Arrays.stream(overlays).map(e -> e.compileOverlay(renderer)).toArray(AbstractMapOverlayRenderer[]::new));
    }

    public void setLayers(AbstractLayerOptions<?>[] layers) {
        this.layers = layers;
        MinimapClientApi.getInstance().inGameHud.getRenderer().setRenderLayers(Arrays.stream(layers).map(AbstractLayerOptions::compileLayer).toArray(AbstractImageStrategy[]::new));
    }

    protected abstract Class<T> getMapRenderer();

    public T compileMapRenderer() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        MapRendererBuilder<T> builder = MapRendererBuilder.createBuilder(getMapRenderer());
        for (AbstractLayerOptions<?> layer : layers) {
            builder.addRenderLayer(layer.compileLayer());
        }
        for (AbstractOverlayOptions<?> overlay : overlays) {
            builder.addOverlay(overlay.compileOverlay(builder.getPartialMapRenderer()));
        }
        return builder.build();
    }
}
