package xyz.wagyourtail.minimap.map.image;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import xyz.wagyourtail.config.field.IntRange;
import xyz.wagyourtail.config.field.Setting;
import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.map.image.colors.AccurateBlockColors;
import xyz.wagyourtail.minimap.map.image.imager.UndergroundImager;

@SettingsContainer("gui.wagyourminimap.setting.layers.accurate_color")
public class UndergroundAccurateImageStrategy extends AccurateBlockColors implements UndergroundImager {

    @Setting("gui.wagyourminimap.setting.layers.underground.light_level")
    @IntRange(from = 1, to = 16)
    public int lightLevel = 8;

    @Override
    public boolean shouldRender() {
        assert UndergroundImager.minecraft.level != null;
        assert UndergroundImager.minecraft.player != null;
        int light = UndergroundImager.minecraft.level.getLightEngine().getLayerListener(LightLayer.SKY).getLightValue(
            UndergroundImager.minecraft.player.blockPosition());
        return light < this.lightLevel;
    }

}
