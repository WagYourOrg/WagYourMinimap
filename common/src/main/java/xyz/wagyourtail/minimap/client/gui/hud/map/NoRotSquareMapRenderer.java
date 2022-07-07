package xyz.wagyourtail.minimap.client.gui.hud.map;

import xyz.wagyourtail.config.field.SettingsContainer;

@SettingsContainer("gui.wagyourminimap.settings.square_norot_style")
public class NoRotSquareMapRenderer extends AbstractSquareMapRenderer {
    public NoRotSquareMapRenderer() {
        super(false);
    }

}
