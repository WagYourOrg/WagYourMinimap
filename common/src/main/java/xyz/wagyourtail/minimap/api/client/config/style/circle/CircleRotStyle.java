package xyz.wagyourtail.minimap.api.client.config.style.circle;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.api.client.config.overlay.AbstractOverlaySettings;
import xyz.wagyourtail.minimap.api.client.config.overlay.ArrowOverlaySettings;
import xyz.wagyourtail.minimap.api.client.config.overlay.CircleMapBorderOverlaySettings;
import xyz.wagyourtail.minimap.api.client.config.overlay.WaypointOverlaySettings;
import xyz.wagyourtail.minimap.api.client.config.overlay.rotate.NorthIconOverlaySettings;
import xyz.wagyourtail.minimap.client.gui.hud.map.CircleMapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.overlay.rotate.NorthIconOverlay;

@SettingsContainer("gui.wagyourminimap.settings.circle_norot_style")
public class CircleRotStyle extends AbstractCircleStyle {
    public CircleRotStyle() {
        super(true);
        availableOverlays.put(NorthIconOverlay.class, NorthIconOverlaySettings.class);

        overlays = new AbstractOverlaySettings[] {
            new CircleMapBorderOverlaySettings(),
            new ArrowOverlaySettings(),
            new WaypointOverlaySettings(),
            new NorthIconOverlaySettings()
        };

    }

    @Override
    protected CircleMapRenderer getMapRenderer() {
        return new CircleMapRenderer(rotate);
    }

}
