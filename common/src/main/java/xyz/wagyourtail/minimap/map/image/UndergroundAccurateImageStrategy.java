package xyz.wagyourtail.minimap.map.image;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import xyz.wagyourtail.minimap.map.image.colors.AccurateBlockColors;
import xyz.wagyourtail.minimap.map.image.imager.UndergroundImager;

public class UndergroundAccurateImageStrategy extends AccurateBlockColors implements UndergroundImager {
    protected final int lightLevel;

    public UndergroundAccurateImageStrategy(int lightLevel) {
        this.lightLevel = lightLevel;
    }

    @Override
    public boolean shouldRender() {
        assert UndergroundImager.minecraft.level != null;
        assert UndergroundImager.minecraft.player != null;
        int light = UndergroundImager.minecraft.level.getLightEngine().getLayerListener(LightLayer.SKY).getLightValue(new BlockPos(UndergroundImager.minecraft.player.getPosition(0)));
        return light < this.lightLevel;
    }
}
