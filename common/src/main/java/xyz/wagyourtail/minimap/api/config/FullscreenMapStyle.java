package xyz.wagyourtail.minimap.api.config;

import xyz.wagyourtail.minimap.api.config.layers.AbstractLayerOptions;
import xyz.wagyourtail.minimap.api.config.layers.LightLayer;
import xyz.wagyourtail.minimap.api.config.layers.VanillaMapLayer;
import xyz.wagyourtail.minimap.client.gui.image.AbstractImageStrategy;
import xyz.wagyourtail.minimap.client.gui.image.BlockLightImageStrategy;
import xyz.wagyourtail.minimap.client.gui.image.VanillaMapImageStrategy;
import xyz.wagyourtail.wagyourconfig.field.Setting;
import xyz.wagyourtail.wagyourconfig.field.SettingsContainer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@SettingsContainer("gui.wagyourminimap.settings.fullscreen_map")
public class FullscreenMapStyle {

//    public Map<Class<? extends AbstractMapOverlayRenderer>, Class<? extends AbstractOverlayOptions>> availableOverlays = new HashMap<>();

    public Map<Class<? extends AbstractImageStrategy>, Class<? extends AbstractLayerOptions>> availableLayers = new HashMap<>();

//    @Setting(value = "gui.wagyourminimap.settings.style.overlay", options = "overlayOptions")
//    public AbstractOverlayOptions<?>[] overlays;

    @Setting(value = "gui.wagyourminimap.settings.style.layers", options = "layerOptions")
    public AbstractLayerOptions<?>[] layers;

    public FullscreenMapStyle() {
        availableLayers.put(VanillaMapImageStrategy.class, VanillaMapLayer.class);
        availableLayers.put(BlockLightImageStrategy.class, LightLayer.class);

//        overlays = new AbstractOverlayOptions[] {};
        layers = new AbstractLayerOptions[] {new VanillaMapLayer(), new LightLayer()};
    }

//    public Collection<Class<? extends AbstractOverlayOptions>> overlayOptions() {
//        return availableOverlays.values();
//    }

    public Collection<Class<? extends AbstractLayerOptions>> layerOptions() {
        return availableLayers.values();
    }
}
