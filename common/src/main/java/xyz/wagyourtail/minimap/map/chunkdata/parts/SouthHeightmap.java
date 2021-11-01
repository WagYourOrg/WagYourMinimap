package xyz.wagyourtail.minimap.map.chunkdata.parts;

import xyz.wagyourtail.minimap.map.chunkdata.ChunkData;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;

public class SouthHeightmap extends DataPart<SouthHeightmap> {
    public final int[] heightmap = new int[16];

    /**
     * empty data
     *
     * @param parent container
     */
    public SouthHeightmap(ChunkData parent) {
        super(parent);
    }

    @Override
    public void mergeFrom(SouthHeightmap other) {
        boolean changed = false;
        for (int i = 0; i < 16; ++i) {
            int newHeight = other.heightmap[i];
            changed = changed || newHeight != this.heightmap[i];
            this.heightmap[i] = newHeight;
        }
        if (changed) parent.markDirty();
    }

    @Override
    public void deserialize(ByteBuffer buffer) {
        for (int i = 0; i < 16; ++i) {
            heightmap[i] = buffer.getInt();
        }
    }

    @Override
    public void serialize(ByteBuffer buffer) {
        for (int i = 0; i < 16; ++i) {
            buffer.putInt(heightmap[i]);
        }
    }

    @Override
    public int getBytes() {
        return Integer.BYTES * 16;
    }

    @Override
    public void usedResourceLocations(Set<Integer> used) {
        //noop
    }

    @Override
    public void remapResourceLocations(Map<Integer, Integer> map) {
        //noop
    }

}
