package xyz.wagyourtail.minimap.map.image;

import net.minecraft.client.renderer.texture.DynamicTexture;
import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.chunkdata.ChunkLocation;

@SettingsContainer("gui.wagyourminimap.setting.layers.none")
public class NullImageStrategy implements ImageStrategy {

    @Override
    public DynamicTexture load(ChunkLocation location, ChunkData data) {
        return null;
    }

    @Override
    public boolean shouldRender() {
        return false;
    }

}
