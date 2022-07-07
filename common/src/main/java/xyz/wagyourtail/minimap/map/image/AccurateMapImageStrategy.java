package xyz.wagyourtail.minimap.map.image;

import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.map.image.colors.AccurateBlockColors;
import xyz.wagyourtail.minimap.map.image.imager.SurfaceImager;

@SettingsContainer("gui.wagyourminimap.setting.layers.accurate_color")
public class AccurateMapImageStrategy extends AccurateBlockColors implements SurfaceImager {
}
