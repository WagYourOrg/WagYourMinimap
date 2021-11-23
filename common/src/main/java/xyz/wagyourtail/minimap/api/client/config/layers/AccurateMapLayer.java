package xyz.wagyourtail.minimap.api.client.config.layers;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.map.image.AccurateMapImageStrategy;

@SettingsContainer("gui.wagyourminimap.setting.layers.accurate_color")
public class AccurateMapLayer extends AbstractLayerOptions<AccurateMapImageStrategy> {
    @Override
    public AccurateMapImageStrategy compileLayer() {
        return new AccurateMapImageStrategy();
    }

}
