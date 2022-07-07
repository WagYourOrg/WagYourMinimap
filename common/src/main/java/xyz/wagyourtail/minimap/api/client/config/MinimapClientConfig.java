package xyz.wagyourtail.minimap.api.client.config;

import xyz.wagyourtail.config.field.IntRange;
import xyz.wagyourtail.config.field.Setting;
import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.client.gui.hud.InGameHud;
import xyz.wagyourtail.minimap.client.gui.hud.map.*;
import xyz.wagyourtail.minimap.client.gui.screen.map.ScreenMapRenderer;
import xyz.wagyourtail.minimap.waypoint.WaypointManager;
import xyz.wagyourtail.minimap.waypoint.filters.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@SettingsContainer("gui.wagyourminimap.settings.client")
public class MinimapClientConfig {
    public static final int MIN_CHUNK_RADIUS = 1;
    public static final int MAX_CHUNK_RADIUS = 30;

    public static Set<Class<? extends AbstractMinimapRenderer>> minimapStyleOptions = new HashSet<>(Set.of(
        RotSquareMapRenderer.class,
        NoRotSquareMapRenderer.class,
        RotCircleMapRenderer.class,
        NoRotCircleMapRenderer.class
    ));
    public static Set<Class<? extends WaypointFilter>> waypointFilterOptions = new HashSet<>(Set.of(
        EnabledFilter.class,
        DistanceFilter.class,
        DimensionFilter.class,
        GroupFilter.class
    ));

    // todo: own implementation of serializedname for old setting names
    public final ScreenMapRenderer fullscreenRenderer = new ScreenMapRenderer();

    @Setting(value = "gui.wagyourminimap.settings.minimap_scale")
    @IntRange(from = 0, to = 100)
    public int minimapScale = 30;

    @Setting(value = "gui.wagyourminimap.settings.map_location")
    public InGameHud.SnapSide snapSide = InGameHud.SnapSide.TOP_RIGHT;

    @Setting(value = "gui.wagyourminimap.settings.chunk_radius", getter = "getChunkRadius", setter = "setChunkRadius")
    @IntRange(from = MIN_CHUNK_RADIUS, to = MAX_CHUNK_RADIUS)
    public int chunkRadius = 5;

    @Setting(value = "gui.wagyourminimap.settings.minimap_style", options = "mapStyles", setter = "setMinimapStyle")
    public AbstractMinimapRenderer minimapRenderer;

    @Setting(value = "gui.wagyourminimap.settings.waypoint_filters",
        options = "waypointFilters",
        setter = "setWaypointFilter")
    public WaypointFilter[] waypointFilters;

    @Setting(value = "gui.wagyourminimap.settings.show_waypoints")
    public boolean showWaypoints = true;

    @Setting(value = "gui.wagyourminimap.settings.show_minimap")
    public boolean showMinimap = true;

    @Setting(value = "gui.wagyourminimap.settings.waypoint_beam")
    public boolean showWaypointBeam = true;

    public MinimapClientConfig() {
        //default style
        setMinimapStyle(new NoRotSquareMapRenderer());
        setWaypointFilter(new WaypointFilter[] {
            new DimensionFilter(), new EnabledFilter(), new DistanceFilter()
        });

    }

    public void setMinimapStyle(AbstractMinimapRenderer renderer) {
        this.minimapRenderer = renderer;
        InGameHud.setRenderer(renderer);
    }

    public void setWaypointFilter(WaypointFilter[] waypointFilters) {
        this.waypointFilters = waypointFilters;
        WaypointManager.clearFilters(false);
        WaypointManager.addFilter(waypointFilters);
    }

    public Collection<Class<? extends AbstractMinimapRenderer>> mapStyles() {
        return minimapStyleOptions;
    }

    public Collection<Class<? extends WaypointFilter>> waypointFilters() {
        return waypointFilterOptions;
    }

    public int getChunkRadius() {
        return chunkRadius - 1;
    }

    public void setChunkRadius(int chunkRadius) {
        this.chunkRadius = chunkRadius + 1;
    }

}
