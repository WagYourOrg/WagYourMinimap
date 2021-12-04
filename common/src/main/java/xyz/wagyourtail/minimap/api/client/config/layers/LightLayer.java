package xyz.wagyourtail.minimap.api.client.config.layers;

import xyz.wagyourtail.config.field.Setting;
import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.map.image.SurfaceBlockLightImageStrategy;

@SettingsContainer("gui.wagyourminimap.setting.layers.light")
public class LightLayer extends AbstractLayerOptions<SurfaceBlockLightImageStrategy> {

    @Setting(value = "gui.wagyourminimap.setting.layers.light.nether")
    public boolean nether = false;

    @Override
    public SurfaceBlockLightImageStrategy compileLayer() {
        return new SurfaceBlockLightImageStrategy(nether);
    }

}
