package xyz.wagyourtail.minimap.client.gui.hud.map;

import xyz.wagyourtail.config.field.SettingsContainer;

@SettingsContainer("gui.wagyourminimap.settings.circle_norot_style")
public class NoRotCircleMapRenderer extends AbstractCircleMapRenderer {
    public NoRotCircleMapRenderer() {
        super(false);
    }

}
