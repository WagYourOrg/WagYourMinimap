package xyz.wagyourtail.minimap.chunkdata.parts;

import xyz.wagyourtail.minimap.chunkdata.ChunkData;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;

public abstract class DataPart<T extends DataPart<?>> {
    public final ChunkData parent;

    /**
     * empty data
     *
     * @param parent container
     */
    public DataPart(ChunkData parent) {
        this.parent = parent;
    }

    public abstract void mergeFrom(T other);

    public abstract void deserialize(ByteBuffer buffer);

    public abstract void serialize(ByteBuffer buffer);

    public abstract int getBytes();

    public abstract void usedBlockStates(Set<Integer> used);

    public abstract void remapBlockStates(Map<Integer, Integer> map);

    public abstract void usedBiomes(Set<Integer> used);

    public abstract void remapBiomes(Map<Integer, Integer> map);
}
