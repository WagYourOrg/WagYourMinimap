package xyz.wagyourtail.minimap.client.gui.hud.map;

import xyz.wagyourtail.config.field.SettingsContainer;

import java.util.Set;

@SettingsContainer("gui.wagyourminimap.settings.circle_rot_style")
public class RotCircleMapRenderer extends AbstractCircleMapRenderer {
    public RotCircleMapRenderer() {
        super(true, Set.of(), Set.of());
    }

}
