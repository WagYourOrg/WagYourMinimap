package xyz.wagyourtail.minimap.client.gui.hud.map;

import xyz.wagyourtail.config.field.SettingsContainer;

import java.util.Set;

@SettingsContainer("gui.wagyourminimap.settings.square_rot_style")
public class RotSquareMapRenderer extends AbstractSquareMapRenderer {
    public RotSquareMapRenderer() {
        super(true, Set.of(), Set.of());
    }
}
