package xyz.wagyourtail.minimap.api.client.config;

import xyz.wagyourtail.config.field.Setting;
import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.api.client.config.layers.AbstractLayerOptions;
import xyz.wagyourtail.minimap.api.client.config.layers.LightLayer;
import xyz.wagyourtail.minimap.api.client.config.layers.VanillaMapLayer;
import xyz.wagyourtail.minimap.api.client.config.overlay.fullscreen.*;
import xyz.wagyourtail.minimap.client.gui.screen.map.*;
import xyz.wagyourtail.minimap.map.image.AbstractImageStrategy;
import xyz.wagyourtail.minimap.map.image.BlockLightImageStrategy;
import xyz.wagyourtail.minimap.map.image.VanillaMapImageStrategy;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SettingsContainer("gui.wagyourminimap.settings.fullscreen_map")
public class FullscreenMapConfig {
    public Map<Class<? extends AbstractImageStrategy>, Class<? extends AbstractLayerOptions>> availableLayers = new ConcurrentHashMap<>();

    public Map<Class<? extends AbstractFullscreenOverlay>, Class<? extends AbstractFullscreenOverlaySettings>> availableOverlays = new ConcurrentHashMap<>();

    @Setting(value = "gui.wagyourminimap.settings.style.layers", options = "layerOptions", setter = "setLayers")
    public AbstractLayerOptions<?>[] layers;

    @Setting(value = "gui.wagyourminimap.settings.style.overlays", options = "overlayOptions", setter = "setOverlays")
    public AbstractFullscreenOverlaySettings<?>[] overlays;

    public FullscreenMapConfig() {
        // overlays = new AbstractOverlayOptions[] {};
        layers = new AbstractLayerOptions[] {new VanillaMapLayer(), new LightLayer()};

        overlays = new AbstractFullscreenOverlaySettings[] {
            new DataOverlaySettings(), new PlayerIconSettings(), new WaypointOverlaySettings()
        };

        availableLayers.put(VanillaMapImageStrategy.class, VanillaMapLayer.class);
        availableLayers.put(BlockLightImageStrategy.class, LightLayer.class);

        availableOverlays.put(DataOverlay.class, DataOverlaySettings.class);
        availableOverlays.put(PlayerIconOverlay.class, PlayerIconSettings.class);
        availableOverlays.put(WaypointOverlay.class, WaypointOverlaySettings.class);
        availableOverlays.put(ScaleOverlay.class, ScaleOverlaySettings.class);
    }

    //    public Collection<Class<? extends AbstractOverlayOptions>> overlayOptions() {
    //        return availableOverlays.values();
    //    }

    public Collection<Class<? extends AbstractLayerOptions>> layerOptions() {
        return availableLayers.values();
    }

    public Collection<Class<? extends AbstractFullscreenOverlaySettings>> overlayOptions() {
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

    public void setOverlays(AbstractFullscreenOverlaySettings[] overlays) {
        this.overlays = overlays;
        try {
            MinimapClientApi.getInstance().screen.renderer.setOverlays(Arrays.stream(overlays)
                .map(AbstractFullscreenOverlaySettings::compileOverlay)
                .toArray(AbstractFullscreenOverlay[]::new));
        } catch (NullPointerException ignored) {
        }
    }

}
