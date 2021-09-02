package xyz.wagyourtail.minimap.api.config.layers;

import xyz.wagyourtail.minimap.client.gui.image.VanillaMapImageStrategy;
import xyz.wagyourtail.wagyourconfig.field.SettingsContainer;

@SettingsContainer("gui.wagyourminimap.setting.layers.vanilla_map")
public class VanillaMapLayer extends AbstractLayerOptions<VanillaMapImageStrategy> {
    @Override
    public VanillaMapImageStrategy compileLayer() {
        return new VanillaMapImageStrategy();
    }

}
