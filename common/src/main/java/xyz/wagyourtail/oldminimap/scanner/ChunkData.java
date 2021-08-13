package xyz.wagyourtail.oldminimap.scanner;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ChunkData {
    public final MapRegion parent;
    public long updateTime;

    public final int[] heightmap = new int[256];
    public final byte[] blocklight = new byte[256];
    public final int[] blockid = new int[256];
    public final int[] biomeid = new int[256];


    public final int[] oceanFloorHeightmap = new int[256];
    public final int[] oceanFloorBlockid = new int[256];
    public final int[] oceanFloorBiomeid = new int[256];

    public final List<ResourceLocation> resources = new ArrayList<>();

    public ChunkData(MapRegion parent) {
        this.parent = parent;
    }
    
    public synchronized ChunkData loadFromDisk(ZipFile file, MapRegion.ZipChunk chunk) {
        try (InputStream stream = file.getInputStream(chunk.data)) {
            ByteBuffer data = ByteBuffer.wrap(stream.readAllBytes());
            data.rewind();
            this.updateTime = data.getLong();
            for (int i = 0; i < 256; ++i) {
                heightmap[i] = data.getInt();
            }
            for (int i = 0; i < 256; ++i) {
                blocklight[i] = data.get();
            }
            for (int i = 0; i < 256; ++i) {
                blockid[i] = data.getInt();
            }
            for (int i = 0; i < 256; ++i) {
                biomeid[i] = data.getInt();
            }
            for (int i = 0; i < 256; ++i) {
                oceanFloorHeightmap[i] = data.getInt();
            }
            for (int i = 0; i < 256; ++i) {
                oceanFloorBlockid[i] = data.getInt();
            }
            for (int i = 0; i < 256; ++i) {
                oceanFloorBiomeid[i] = data.getInt();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try (InputStream stream = file.getInputStream(chunk.resources)) {
            for (String resource : new String(stream.readAllBytes()).split("\n")) {
                resources.add(new ResourceLocation(resource));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return this;
    }

    public synchronized void writeToZip(ZipOutputStream out, String pos_slug) {
        try {
            ByteBuffer data = ByteBuffer.allocate(Long.BYTES + Integer.BYTES * 256 * 6 + Byte.BYTES * 256);
            data.putLong(updateTime);
            for (int i = 0; i < 256; ++i) {
                data.putInt(heightmap[i]);
            }
            for (int i = 0; i < 256; ++i) {
                data.put((byte) blocklight[i]);
            }
            for (int i = 0; i < 256; ++i) {
                data.putInt(blockid[i]);
            }
            for (int i = 0; i < 256; ++i) {
                data.putInt(biomeid[i]);
            }
            for (int i = 0; i < 256; ++i) {
                data.putInt(oceanFloorHeightmap[i]);
            }
            for (int i = 0; i < 256; ++i) {
                data.putInt(oceanFloorBlockid[i]);
            }
            for (int i = 0; i < 256; ++i) {
                data.putInt(oceanFloorBiomeid[i]);
            }
            out.putNextEntry(new ZipEntry(pos_slug + ".data"));
            out.write(data.array());
            String resources = this.resources.stream().map(ResourceLocation::toString).reduce("", (a, b) -> a + b + "\n");
            out.putNextEntry(new ZipEntry(pos_slug + ".resources"));
            out.write(resources.getBytes(StandardCharsets.UTF_8));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public int getOrRegisterResourceLocation(ResourceLocation id) {
        if (id == null) return -1;
        for (int j = 0; j < resources.size(); ++j) {
            if (id.equals(resources.get(j))) {
                return j;
            }
        }
        int k = resources.size();
        resources.add(id);
        return k;
    }

    public static int blockPosToIndex(BlockPos pos) {
        int x = pos.getX() % 16;
        int z = pos.getZ() % 16;
        if (x < 0) x += 16;
        if (z < 0) z += 16;
        return (x << 4) + z;
    }
}
