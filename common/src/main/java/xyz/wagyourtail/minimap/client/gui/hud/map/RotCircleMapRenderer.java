package xyz.wagyourtail.minimap.client.gui.hud.map;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.client.gui.hud.overlay.rotate.NorthIconOverlay;

import java.util.Set;

@SettingsContainer("gui.wagyourminimap.settings.circle_rot_style")
public class RotCircleMapRenderer extends AbstractCircleMapRenderer {
    public RotCircleMapRenderer() {
        super(true, Set.of(), Set.of());
    }

}
