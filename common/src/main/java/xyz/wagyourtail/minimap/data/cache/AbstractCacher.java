package xyz.wagyourtail.minimap.data.cache;

import xyz.wagyourtail.minimap.data.ChunkData;
import xyz.wagyourtail.minimap.data.ChunkLocation;

public abstract class AbstractCacher {
    public abstract ChunkData load(ChunkLocation location);

    public abstract void save(ChunkLocation location, ChunkData data);
}
