package xyz.wagyourtail.minimap.api.client.config.style;

import xyz.wagyourtail.config.field.Setting;
import xyz.wagyourtail.minimap.api.client.config.layers.AbstractLayerOptions;
import xyz.wagyourtail.minimap.api.client.config.layers.LightLayer;
import xyz.wagyourtail.minimap.api.client.config.layers.VanillaMapLayer;
import xyz.wagyourtail.minimap.api.client.config.overlay.AbstractOverlaySettings;
import xyz.wagyourtail.minimap.api.client.config.overlay.ArrowOverlaySettings;
import xyz.wagyourtail.minimap.api.client.config.overlay.WaypointOverlaySettings;
import xyz.wagyourtail.minimap.client.gui.MapRendererBuilder;
import xyz.wagyourtail.minimap.client.gui.hud.InGameHud;
import xyz.wagyourtail.minimap.client.gui.hud.map.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.map.WaypointOverlay;
import xyz.wagyourtail.minimap.client.gui.hud.overlay.AbstractMinimapOverlay;
import xyz.wagyourtail.minimap.client.gui.hud.overlay.PlayerArrowOverlay;
import xyz.wagyourtail.minimap.map.image.AbstractImageStrategy;
import xyz.wagyourtail.minimap.map.image.BlockLightImageStrategy;
import xyz.wagyourtail.minimap.map.image.VanillaMapImageStrategy;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@SuppressWarnings("rawtypes")
public abstract class AbstractMinimapStyle<T extends AbstractMinimapRenderer> {
    public Map<Class<? extends AbstractMinimapOverlay>, Class<? extends AbstractOverlaySettings>> availableOverlays = new ConcurrentHashMap<>();

    public Map<Class<? extends AbstractImageStrategy>, Class<? extends AbstractLayerOptions>> availableLayers = new ConcurrentHashMap<>();

    @Setting(value = "gui.wagyourminimap.settings.style.overlay", options = "overlayOptions", setter = "setOverlays")
    public AbstractOverlaySettings<?>[] overlays;

    @Setting(value = "gui.wagyourminimap.settings.style.layers", options = "layerOptions", setter = "setLayers")
    public AbstractLayerOptions<?>[] layers;

    public AbstractMinimapStyle() {
        // default layers
        layers = new AbstractLayerOptions[] {new VanillaMapLayer(), new LightLayer()};

        //layer register
        availableLayers.put(VanillaMapImageStrategy.class, VanillaMapLayer.class);
        availableLayers.put(BlockLightImageStrategy.class, LightLayer.class);

        //overlay register
        availableOverlays.put(PlayerArrowOverlay.class, ArrowOverlaySettings.class);
        availableOverlays.put(WaypointOverlay.class, WaypointOverlaySettings.class);
    }

    public Collection<Class<? extends AbstractOverlaySettings>> overlayOptions() {
        return availableOverlays.values();
    }

    public Collection<Class<? extends AbstractLayerOptions>> layerOptions() {
        return availableLayers.values();
    }

    public void setOverlays(AbstractOverlaySettings<?>[] overlays) {
        this.overlays = overlays;
        AbstractMinimapRenderer renderer = InGameHud.getRenderer();
        renderer.setOverlays(Arrays.stream(overlays)
            .map(e -> e.compileOverlay(renderer))
            .toArray(AbstractMinimapOverlay[]::new));
    }

    public void setLayers(AbstractLayerOptions<?>[] layers) {
        this.layers = layers;
        InGameHud.getRenderer().setRenderLayers(Arrays.stream(layers)
            .map(AbstractLayerOptions::compileLayer)
            .toArray(AbstractImageStrategy[]::new));
    }

    public T compileMapRenderer() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        MapRendererBuilder<T> builder = MapRendererBuilder.createBuilder(getMapRenderer());
        for (AbstractLayerOptions<?> layer : layers) {
            builder.addRenderLayer(layer.compileLayer());
        }
        for (AbstractOverlaySettings<?> overlay : overlays) {
            builder.addOverlay(overlay.compileOverlay(builder.getPartialMapRenderer()));
        }
        return builder.build();
    }

    protected abstract T getMapRenderer();

}
