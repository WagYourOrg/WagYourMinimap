package xyz.wagyourtail.minimap.api.client.config.style.circle;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.api.client.config.overlay.AbstractOverlaySettings;
import xyz.wagyourtail.minimap.api.client.config.overlay.ArrowOverlaySettings;
import xyz.wagyourtail.minimap.api.client.config.overlay.CircleMapBorderOverlaySettings;
import xyz.wagyourtail.minimap.api.client.config.overlay.WaypointOverlaySettings;
import xyz.wagyourtail.minimap.client.gui.hud.map.CircleMapRenderer;

@SettingsContainer("gui.wagyourminimap.settings.circle_rot_style")
public class CircleNoRotStyle extends AbstractCircleStyle {
    public CircleNoRotStyle() {
        super();

        overlays = new AbstractOverlaySettings[] {
            new CircleMapBorderOverlaySettings(),
            new ArrowOverlaySettings(),
            new WaypointOverlaySettings(),
        };

    }

    @Override
    protected CircleMapRenderer getMapRenderer() {
        return new CircleMapRenderer(false);
    }

}
