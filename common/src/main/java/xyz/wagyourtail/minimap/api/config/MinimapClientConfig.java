package xyz.wagyourtail.minimap.api.config;

import xyz.wagyourtail.minimap.client.gui.InGameHud;

@SettingsContainer
public class MinimapClientConfig {
    @Setting(value = "gui.wagyourminimap.settings.minimap_scale")
    @IntRange(from = 0, to = 100)
    public int minimapScale = 30;

    @Setting(value = "gui.wagyourminimap.settings.map_location")
    public InGameHud.SnapSide snapSide = InGameHud.SnapSide.TOP_RIGHT;

    @Setting(value = "gui.wagyourminimap.settings.chunk_radius")
    public int chunkRadius = 5;
}
