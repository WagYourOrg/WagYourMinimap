package xyz.wagyourtail.minimap.chunkdata.updater;

import net.minecraft.core.SectionPos;
import net.minecraft.world.level.chunk.ChunkSource;

public interface LightLevelSetEvent {
    void onLightLevel(ChunkSource chunkGetter, SectionPos pos);
}
