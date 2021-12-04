package xyz.wagyourtail.minimap.api.client.config.layers;

import xyz.wagyourtail.config.field.IntRange;
import xyz.wagyourtail.config.field.Setting;
import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.map.image.UndergroundBlockLightImageStrategy;

@SettingsContainer("gui.wagyourminimap.setting.layers.underground.light")
public class UndergroundLightLayer extends AbstractLayerOptions<UndergroundBlockLightImageStrategy> {
    @Setting("gui.wagyourminimap.setting.layers.underground.light_level")
    @IntRange(from = 0, to = 15)
    public int lightLevel = 7;

    @Override
    public UndergroundBlockLightImageStrategy compileLayer() {
        return new UndergroundBlockLightImageStrategy(lightLevel + 1);
    }

}
