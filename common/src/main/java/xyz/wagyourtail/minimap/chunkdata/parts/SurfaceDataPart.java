package xyz.wagyourtail.minimap.chunkdata.parts;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import xyz.wagyourtail.minimap.chunkdata.ChunkData;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;

public class SurfaceDataPart extends DataPart<SurfaceDataPart> {
    private final int dataVersion = 1;
    public final int[] heightmap = new int[256];
    public final int[] blockid = new int[256];
    public final int[] biomeid = new int[256];
    public final int[] oceanFloorHeightmap = new int[256];
    public final int[] oceanFloorBlockid = new int[256];

    public SurfaceDataPart(ChunkData parent) {
        super(parent);
    }

    public int getDataVersion() {
        return dataVersion;
    }

    public static int blockPosToIndex(int posX, int posZ) {
        int x = posX % 16;
        int z = posZ % 16;
        if (x < 0) {
            x += 16;
        }
        if (z < 0) {
            z += 16;
        }
        return (x << 4) + z;
    }

    public static int blockPosToIndex(BlockPos pos) {
        int x = pos.getX() % 16;
        int z = pos.getZ() % 16;
        if (x < 0) {
            x += 16;
        }
        if (z < 0) {
            z += 16;
        }
        return (x << 4) + z;
    }

    @Override
    public boolean mergeFrom(SurfaceDataPart other) {
        if (other.parent.updateTime >= this.parent.updateTime) {
            this.parent.updateTime = other.parent.updateTime;
            this.parent.changed = true;
            boolean changed = false;
            Map<Integer, Integer> idmap = new Int2IntOpenHashMap();
            for (int i = 0; i < 256; ++i) {
                int newHeight = other.heightmap[i];
                changed = changed || newHeight != this.heightmap[i];
                this.heightmap[i] = newHeight;

                int newBlockid = idmap.computeIfAbsent(
                    other.blockid[i],
                    k -> parent.getOrRegisterBlockState(other.parent.getBlockState(k))
                );
                changed = changed || newBlockid != this.blockid[i];
                this.blockid[i] = newBlockid;

                int newBiomeId = other.biomeid[i];
                changed = changed || newBiomeId != this.biomeid[i];
                this.biomeid[i] = newBiomeId;

                int newOceanHeight = other.oceanFloorHeightmap[i];
                changed = changed || newOceanHeight != this.oceanFloorHeightmap[i];
                this.oceanFloorHeightmap[i] = newOceanHeight;

                int newOceanBlock = other.oceanFloorBlockid[i];
                changed = changed || newOceanBlock != this.oceanFloorBlockid[i];
                this.oceanFloorBlockid[i] = newOceanBlock;
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
        if (dataVersion == 1 && getBytes() == size) {
            for (int i = 0; i < 256; ++i) {
                this.heightmap[i] = buffer.getInt();
                this.blockid[i] = buffer.getInt();
                this.biomeid[i] = buffer.getInt();
                this.oceanFloorHeightmap[i] = buffer.getInt();
                this.oceanFloorBlockid[i] = buffer.getInt();
            }
        } else if (size == Integer.BYTES * 256 * 5 + 256) {
            buffer.position(buffer.position() - Integer.BYTES);
            byte[] blocklight = new byte[256];
            for (int i = 0; i < 256; ++i) {
                this.heightmap[i] = buffer.getInt();
                blocklight[i] = buffer.get();
                this.blockid[i] = buffer.getInt();
                this.biomeid[i] = buffer.getInt();
                this.oceanFloorHeightmap[i] = buffer.getInt();
                this.oceanFloorBlockid[i] = buffer.getInt();
            }
            parent.computeData(LightDataPart.class, (v) -> LightDataPart.fromSurfaceV0(parent, blocklight));
        } else {
            throw new IllegalArgumentException("Invalid surface data: " + dataVersion + " " + size);
        }
    }

    @Override
    public void serialize(ByteBuffer buffer) {
        buffer.putInt(dataVersion);
        for (int i = 0; i < 256; ++i) {
            buffer.putInt(this.heightmap[i]);
            buffer.putInt(this.blockid[i]);
            buffer.putInt(this.biomeid[i]);
            buffer.putInt(this.oceanFloorHeightmap[i]);
            buffer.putInt(this.oceanFloorBlockid[i]);
        }
    }

    @Override
    public int getBytes() {
        return Integer.BYTES + Integer.BYTES * 256 * 5;
    }

    @Override
    public void usedBlockStates(Set<Integer> used) {
        for (int i = 0; i < 256; ++i) {
            used.add(this.blockid[i]);
            used.add(this.oceanFloorBlockid[i]);
        }
    }

    @Override
    public synchronized void remapBlockStates(Map<Integer, Integer> map) {
        for (int i = 0; i < 256; ++i) {
            int blockid = this.blockid[i];
            int newBlockid = map.computeIfAbsent(blockid, (k) -> {
                throw new NullPointerException("No mapping for blockid " + k);
            });
            this.blockid[i] = newBlockid;
        }
    }

    @Override
    public void usedBiomes(Set<Integer> used) {
        for (int i = 0; i < 256; ++i) {
            used.add(this.biomeid[i]);
        }
    }

    @Override
    public void remapBiomes(Map<Integer, Integer> map) {
        for (int i = 0; i < 256; ++i) {
            int biomeid = this.biomeid[i];
            int newBiomeid = map.computeIfAbsent(biomeid, (k) -> {
                throw new NullPointerException("No mapping for biomeid " + k);
            });
            this.biomeid[i] = newBiomeid;
        }
    }

}
