package xyz.wagyourtail.minimap.api.client.config;

import xyz.wagyourtail.config.field.IntRange;
import xyz.wagyourtail.config.field.Setting;
import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.api.client.config.circle.norot.CircleNoRotStyle;
import xyz.wagyourtail.minimap.api.client.config.circle.rot.CircleRotStyle;
import xyz.wagyourtail.minimap.api.client.config.square.norot.SquareNoRotStyle;
import xyz.wagyourtail.minimap.api.client.config.square.rot.SquareRotStyle;
import xyz.wagyourtail.minimap.api.client.config.waypointfilter.*;
import xyz.wagyourtail.minimap.client.gui.AbstractMapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.InGameHud;
import xyz.wagyourtail.minimap.client.gui.hud.map.circle.norot.CircleMapNoRotRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.map.circle.rotate.CircleMapRotRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.map.square.norot.SquareMapNoRotRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.map.square.rotate.SquareMapRotRenderer;
import xyz.wagyourtail.minimap.waypoint.WaypointManager;
import xyz.wagyourtail.minimap.waypoint.filters.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SettingsContainer("gui.wagyourminimap.settings.client")
public class MinimapClientConfig {

    public static Map<Class<? extends AbstractMapRenderer>, Class<? extends AbstractMinimapStyle>> minimapStyleOptions = new ConcurrentHashMap<>();
    public static Map<Class<? extends WaypointFilter>, Class<? extends AbstractWaypointFilterOptions>> waypointFilterOptions = new ConcurrentHashMap<>();

    static {
        minimapStyleOptions.put(SquareMapNoRotRenderer.class, SquareNoRotStyle.class);
        minimapStyleOptions.put(SquareMapRotRenderer.class, SquareRotStyle.class);
        minimapStyleOptions.put(CircleMapNoRotRenderer.class, CircleNoRotStyle.class);
        minimapStyleOptions.put(CircleMapRotRenderer.class, CircleRotStyle.class);

        waypointFilterOptions.put(EnabledFilter.class, EnabledFilterOptions.class);
        waypointFilterOptions.put(DistanceFilter.class, DistanceFilterOptions.class);
        waypointFilterOptions.put(DimensionFilter.class, DimensionFilterOptions.class);
        waypointFilterOptions.put(GroupFilter.class, GroupFilterOptions.class);
    }

    public final FullscreenMapStyle fullscreenMapStyle = new FullscreenMapStyle();

    @Setting(value = "gui.wagyourminimap.settings.minimap_scale")
    @IntRange(from = 0, to = 100)
    public int minimapScale = 30;

    @Setting(value = "gui.wagyourminimap.settings.map_location")
    public InGameHud.SnapSide snapSide = InGameHud.SnapSide.TOP_RIGHT;

    @Setting(value = "gui.wagyourminimap.settings.chunk_radius", getter = "getChunkRadius", setter = "setChunkRadius")
    @IntRange(from = 1, to = 30)
    public int chunkRadius = 5;
    @Setting(value = "gui.wagyourminimap.settings.minimap_style", options = "mapStyles", setter = "setMinimapStyle")
    public AbstractMinimapStyle<?> style;

    @Setting(value = "gui.wagyourminimap.settings.waypoint_filters",
        options = "waypointFilters",
        setter = "setWaypointFilter")
    public AbstractWaypointFilterOptions<?>[] waypointFilters;

    @Setting(value = "gui.wagyourminimap.settings.show_waypoints")
    public boolean showWaypoints = true;

    @Setting(value = "gui.wagyourminimap.settings.waypoint_beam")
    public boolean showWaypointBeam = true;

    public MinimapClientConfig() {
        //default style
        setMinimapStyle(new SquareNoRotStyle());
        setWaypointFilter(new AbstractWaypointFilterOptions[] {
            new DimensionFilterOptions(), new EnabledFilterOptions(), new DistanceFilterOptions()
        });

    }

    public void setMinimapStyle(AbstractMinimapStyle<?> style) {
        this.style = style;
        try {
            InGameHud.setRenderer(style.compileMapRenderer());
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void setWaypointFilter(AbstractWaypointFilterOptions<?>[] waypointFilters) {
        this.waypointFilters = waypointFilters;
        WaypointManager.clearFilters(false);
        WaypointManager.addFilter(Arrays.stream(waypointFilters)
            .map(AbstractWaypointFilterOptions::compileFilter)
            .toArray(WaypointFilter[]::new));
    }

    public Collection<Class<? extends AbstractMinimapStyle>> mapStyles() {
        return minimapStyleOptions.values();
    }

    public Collection<Class<? extends AbstractWaypointFilterOptions>> waypointFilters() {
        return waypointFilterOptions.values();
    }

    public int getChunkRadius() {
        return chunkRadius - 1;
    }

    public void setChunkRadius(int chunkRadius) {
        this.chunkRadius = chunkRadius + 1;
    }

}
