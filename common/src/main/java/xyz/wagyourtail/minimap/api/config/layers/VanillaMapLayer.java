package xyz.wagyourtail.minimap.api.config.layers;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.client.gui.image.VanillaMapImageStrategy;

@SettingsContainer("gui.wagyourminimap.setting.layers.vanilla_map")
public class VanillaMapLayer extends AbstractLayerOptions<VanillaMapImageStrategy> {
    @Override
    public VanillaMapImageStrategy compileLayer() {
        return new VanillaMapImageStrategy();
    }

}
