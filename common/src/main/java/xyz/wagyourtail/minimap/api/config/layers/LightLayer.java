package xyz.wagyourtail.minimap.api.config.layers;

import xyz.wagyourtail.minimap.client.gui.image.BlockLightImageStrategy;
import xyz.wagyourtail.config.field.SettingsContainer;

@SettingsContainer("gui.wagyourminimap.setting.layers.light")
public class LightLayer extends AbstractLayerOptions<BlockLightImageStrategy> {
    @Override
    public BlockLightImageStrategy compileLayer() {
        return new BlockLightImageStrategy();
    }

}
