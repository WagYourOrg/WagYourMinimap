package xyz.wagyourtail.minimap.api.config.circle.rot;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.api.config.AbstractOverlayOptions;
import xyz.wagyourtail.minimap.api.config.RotArrowOverlayOptions;
import xyz.wagyourtail.minimap.api.config.circle.AbstractCircleStyle;
import xyz.wagyourtail.minimap.api.config.circle.CircleMapBorderOverlaySettings;
import xyz.wagyourtail.minimap.client.gui.hud.map.circle.rotate.CircleMapRotNorthIcon;
import xyz.wagyourtail.minimap.client.gui.hud.map.circle.rotate.CircleMapRotRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.map.circle.rotate.CircleMapRotWaypointOverlay;
import xyz.wagyourtail.minimap.client.gui.hud.map.RotPlayerArrowOverlay;

@SettingsContainer("gui.wagyourminimap.settings.circle_rot_style")
public class CircleRotStyle extends AbstractCircleStyle<CircleMapRotRenderer> {
    public CircleRotStyle() {
        super();
        availableOverlays.put(RotPlayerArrowOverlay.class, RotArrowOverlayOptions.class);
        availableOverlays.put(CircleMapRotNorthIcon.class, CircleMapRotNorthOverlaySettings.class);
        availableOverlays.put(CircleMapRotWaypointOverlay.class, CircleMapRotWaypointOverlaySettings.class);

        overlays = new AbstractOverlayOptions[] {new RotArrowOverlayOptions(), new CircleMapBorderOverlaySettings(), new CircleMapRotNorthOverlaySettings(), new CircleMapRotWaypointOverlaySettings()};

    }

    @Override
    protected Class<CircleMapRotRenderer> getMapRenderer() {
        return CircleMapRotRenderer.class;
    }

}
