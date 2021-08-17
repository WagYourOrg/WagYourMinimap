package xyz.wagyourtail.minimap.chunkdata.cache;

import xyz.wagyourtail.minimap.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.chunkdata.ChunkLocation;

public abstract class AbstractCacher {
    public abstract ChunkData load(ChunkLocation location);

    public abstract void save(ChunkLocation location, ChunkData data);
}
