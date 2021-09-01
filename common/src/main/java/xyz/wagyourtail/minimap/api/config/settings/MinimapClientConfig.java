package xyz.wagyourtail.minimap.api.config.settings;

import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.api.config.IntRange;
import xyz.wagyourtail.minimap.api.config.Setting;
import xyz.wagyourtail.minimap.api.config.SettingsContainer;
import xyz.wagyourtail.minimap.api.config.settings.square.norot.SquareNoRotStyle;
import xyz.wagyourtail.minimap.client.gui.InGameHud;
import xyz.wagyourtail.minimap.client.gui.renderer.AbstractMapRenderer;
import xyz.wagyourtail.minimap.client.gui.renderer.square.norot.SquareMapNoRotRenderer;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@SettingsContainer
public class MinimapClientConfig {

    @Setting(value = "gui.wagyourminimap.settings.minimap_scale")
    @IntRange(from = 0, to = 100)
    public int minimapScale = 30;

    @Setting(value = "gui.wagyourminimap.settings.map_location")
    public InGameHud.SnapSide snapSide = InGameHud.SnapSide.TOP_RIGHT;

    @Setting(value = "gui.wagyourminimap.settings.chunk_radius")
    public int chunkRadius = 5;

    @Setting(value = "gui.wagyourminimap.settings.minimap_style", options = "mapStyles", setter = "setMinimapStyle")
    public AbstractMinimapStyle<?> style;

    @Setting(value = "gui.wagyourminimap.settings.show_waypoints")
    public boolean showWaypoints = true;

    public static Map<Class<? extends AbstractMapRenderer>, Class<? extends AbstractMinimapStyle>> minimapStyleOptions = new HashMap<>();

    public Collection<Class<? extends AbstractMinimapStyle>> mapStyles() {
        return minimapStyleOptions.values();
    }

    public void setMinimapStyle(AbstractMinimapStyle<?> style) {
        this.style = style;
        try {
            MinimapClientApi.getInstance().inGameHud.setRenderer(style.compileMapRenderer());
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public MinimapClientConfig() {
        //default style
        setMinimapStyle(new SquareNoRotStyle());
    }

    static {
        minimapStyleOptions.put(SquareMapNoRotRenderer.class, SquareNoRotStyle.class);
    }

}
