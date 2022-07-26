package xyz.wagyourtail.minimap.chunkdata.parts;

import xyz.wagyourtail.minimap.chunkdata.ChunkData;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;

public class LightDataPart extends DataPart<LightDataPart> {

    public int dataVersion = 0;
    public final byte[] blocklight = new byte[256];

    public LightDataPart(ChunkData parent) {
        super(parent);
    }

    @Override
    public int getDataVersion() {
        return dataVersion;
    }

    @Override
    public boolean mergeFrom(LightDataPart other) {
        if (other.parent.updateTime >= this.parent.updateTime) {
            this.parent.updateTime = other.parent.updateTime;

            boolean changed = false;
            for (int i = 0; i < 256; ++i) {
                byte newLight = other.blocklight[i];
                changed = changed || newLight != this.blocklight[i];
                this.blocklight[i] = newLight;
            }

            if (changed) {
                parent.markDirty();
            }
            return changed;
        }
        return false;
    }

    @Override
    public void deserialize(ByteBuffer buffer, int size) {
        int dataVersion = buffer.getInt();
        if (dataVersion == 0 && size == getBytes()) {
            for (int i = 0; i < 256; ++i) {
                blocklight[i] = buffer.get();
            }
        } else {
            throw new IllegalArgumentException("LightDataPart: invalid " + dataVersion + " " + size);
        }
    }

    @Override
    public void serialize(ByteBuffer buffer) {
        buffer.putInt(dataVersion);
        for (int i = 0; i < 256; ++i) {
            buffer.put(blocklight[i]);
        }
    }

    @Override
    public int getBytes() {
        return Integer.BYTES + 256;
    }

    @Override
    public void usedBlockStates(Set<Integer> used) {
    }

    @Override
    public void remapBlockStates(Map<Integer, Integer> map) {
    }

    @Override
    public void usedBiomes(Set<Integer> used) {
    }

    @Override
    public void remapBiomes(Map<Integer, Integer> map) {
    }

    public static LightDataPart fromSurfaceV0(ChunkData parent, byte[] blocklight) {
        LightDataPart lightData = new LightDataPart(parent);
        System.arraycopy(blocklight, 0, lightData.blocklight, 0, 256);
        return lightData;
    }

}
