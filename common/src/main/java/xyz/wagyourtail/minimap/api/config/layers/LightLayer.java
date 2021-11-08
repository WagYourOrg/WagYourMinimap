package xyz.wagyourtail.minimap.api.config.layers;

import xyz.wagyourtail.config.field.Setting;
import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.map.image.BlockLightImageStrategy;

@SettingsContainer("gui.wagyourminimap.setting.layers.light")
public class LightLayer extends AbstractLayerOptions<BlockLightImageStrategy> {

    @Setting(value = "gui.wagyourminimap.setting.layers.light.nether")
    public boolean nether = false;

    @Override
    public BlockLightImageStrategy compileLayer() {
        return new BlockLightImageStrategy(nether);
    }

}
