package xyz.wagyourtail.minimap.api.client.config.style.circle;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.api.client.config.overlay.*;
import xyz.wagyourtail.minimap.client.gui.hud.map.CircleMapRenderer;

@SettingsContainer("gui.wagyourminimap.settings.circle_rot_style")
public class CircleNoRotStyle extends AbstractCircleStyle {
    public CircleNoRotStyle() {
        super(false);

        overlays = new AbstractOverlaySettings[] {
            new CircleMapBorderOverlaySettings(),
            new ArrowOverlaySettings(),
            new WaypointOverlaySettings(),
            new MobIconOverlaySettings()
        };

    }

    @Override
    protected CircleMapRenderer getMapRenderer() {
        return new CircleMapRenderer(rotate);
    }

}
