package xyz.wagyourtail.minimap.api.client.config.circle.norot;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.api.client.config.AbstractOverlayOptions;
import xyz.wagyourtail.minimap.api.client.config.NoRotArrowOverlayOptions;
import xyz.wagyourtail.minimap.api.client.config.circle.AbstractCircleStyle;
import xyz.wagyourtail.minimap.api.client.config.circle.CircleMapBorderOverlaySettings;
import xyz.wagyourtail.minimap.client.gui.hud.map.circle.norot.CircleMapNoRotRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.map.circle.norot.CircleMapNoRotWaypointOverlay;
import xyz.wagyourtail.minimap.client.gui.hud.map.NoRotPlayerArrowOverlay;

@SettingsContainer("gui.wagyourminimap.settings.circle_norot_style")
public class CircleNoRotStyle extends AbstractCircleStyle<CircleMapNoRotRenderer> {
    public CircleNoRotStyle() {
        super();
        availableOverlays.put(NoRotPlayerArrowOverlay.class, NoRotArrowOverlayOptions.class);
        availableOverlays.put(CircleMapNoRotWaypointOverlay.class, CircleMapNoRotWaypointOverlaySettings.class);

        overlays = new AbstractOverlayOptions[] {new NoRotArrowOverlayOptions(), new CircleMapBorderOverlaySettings(), new CircleMapNoRotWaypointOverlaySettings()};

    }

    @Override
    protected Class<CircleMapNoRotRenderer> getMapRenderer() {
        return CircleMapNoRotRenderer.class;
    }

}
