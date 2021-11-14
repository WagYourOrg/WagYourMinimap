package xyz.wagyourtail.minimap.api.client.config;

import xyz.wagyourtail.config.field.Setting;
import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.api.client.config.fullscreenoverlays.*;
import xyz.wagyourtail.minimap.api.client.config.layers.AbstractLayerOptions;
import xyz.wagyourtail.minimap.api.client.config.layers.LightLayer;
import xyz.wagyourtail.minimap.api.client.config.layers.VanillaMapLayer;
import xyz.wagyourtail.minimap.client.gui.screen.map.*;
import xyz.wagyourtail.minimap.map.image.AbstractImageStrategy;
import xyz.wagyourtail.minimap.map.image.BlockLightImageStrategy;
import xyz.wagyourtail.minimap.map.image.VanillaMapImageStrategy;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SettingsContainer("gui.wagyourminimap.settings.fullscreen_map")
public class FullscreenMapStyle {
    public Map<Class<? extends AbstractImageStrategy>, Class<? extends AbstractLayerOptions>> availableLayers = new ConcurrentHashMap<>();

    public Map<Class<? extends AbstractFullscreenOverlay>, Class<? extends AbstractFullscreenOverlayOptions>> availableOverlays = new ConcurrentHashMap<>();

    @Setting(value = "gui.wagyourminimap.settings.style.layers", options = "layerOptions", setter = "setLayers")
    public AbstractLayerOptions<?>[] layers;

    @Setting(value = "gui.wagyourminimap.settings.style.overlays", options = "overlayOptions", setter = "setOverlays")
    public AbstractFullscreenOverlayOptions<?>[] overlays;

    public FullscreenMapStyle() {
        availableLayers.put(VanillaMapImageStrategy.class, VanillaMapLayer.class);
        availableLayers.put(BlockLightImageStrategy.class, LightLayer.class);

        availableOverlays.put(DataOverlay.class, DataOverlayOptions.class);
        availableOverlays.put(PlayerIconOverlay.class, PlayerIconOptions.class);
        availableOverlays.put(WaypointOverlay.class, WaypointOverlayOptions.class);
        availableOverlays.put(ScaleOverlay.class, ScaleOverlayOptions.class);

        //        overlays = new AbstractOverlayOptions[] {};
        layers = new AbstractLayerOptions[] {new VanillaMapLayer(), new LightLayer()};

        overlays = new AbstractFullscreenOverlayOptions[] {
            new DataOverlayOptions(), new PlayerIconOptions(), new WaypointOverlayOptions()
        };
    }

    //    public Collection<Class<? extends AbstractOverlayOptions>> overlayOptions() {
    //        return availableOverlays.values();
    //    }

    public Collection<Class<? extends AbstractLayerOptions>> layerOptions() {
        return availableLayers.values();
    }

    public Collection<Class<? extends AbstractFullscreenOverlayOptions>> overlayOptions() {
        return availableOverlays.values();
    }

    public void setLayers(AbstractLayerOptions[] layers) {
        this.layers = layers;
        try {
            MinimapClientApi.getInstance().screen.renderer.setRenderLayers(Arrays.stream(layers)
                .map(AbstractLayerOptions::compileLayer)
                .toArray(AbstractImageStrategy[]::new));
        } catch (NullPointerException ignored) {
        }
    }

    public void setOverlays(AbstractFullscreenOverlayOptions[] overlays) {
        this.overlays = overlays;
        try {
            MinimapClientApi.getInstance().screen.renderer.setOverlays(Arrays.stream(overlays)
                .map(AbstractFullscreenOverlayOptions::compileOverlay)
                .toArray(AbstractFullscreenOverlay[]::new));
        } catch (NullPointerException ignored) {
        }
    }

}
