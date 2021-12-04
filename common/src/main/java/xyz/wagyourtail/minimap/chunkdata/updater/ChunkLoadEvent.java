package xyz.wagyourtail.minimap.chunkdata.updater;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;

public interface ChunkLoadEvent {
    void onLoadChunk(ChunkAccess chunk, Level level);

}
