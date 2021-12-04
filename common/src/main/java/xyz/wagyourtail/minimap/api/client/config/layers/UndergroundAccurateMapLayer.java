package xyz.wagyourtail.minimap.api.client.config.layers;

import xyz.wagyourtail.config.field.IntRange;
import xyz.wagyourtail.config.field.Setting;
import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.map.image.UndergroundAccurateImageStrategy;

@SettingsContainer("gui.wagyourminimap.setting.layers.underground.accurate")
public class UndergroundAccurateMapLayer extends AbstractLayerOptions<UndergroundAccurateImageStrategy> {
    @Setting("gui.wagyourminimap.setting.layers.underground.light_level")
    @IntRange(from = 0, to = 15)
    public int lightLevel = 7;

    @Override
    public UndergroundAccurateImageStrategy compileLayer() {
        return new UndergroundAccurateImageStrategy(lightLevel + 1);
    }

}
