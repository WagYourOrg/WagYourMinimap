package xyz.wagyourtail.minimap.scanner;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class MapRegion {
    public static final int REGION_SIZE = 32;
    public static final int REGION_SQUARE_SIZE = 1024;
    public final MapLevel parent;
    public final MapLevel.Pos position;
    public final ChunkData[] data = new ChunkData[REGION_SQUARE_SIZE];
    public final boolean[] datalocks = new boolean[REGION_SQUARE_SIZE];

    public MapRegion(MapLevel parent, MapLevel.Pos pos) {
        this.parent = parent;
        this.position = pos;
    }

    public void readRegion(Path file) throws IOException {
        ZipChunk[] zipData = new ZipChunk[REGION_SQUARE_SIZE];
        try (ZipFile zf = new ZipFile(file.toFile())) {
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                ZipEntry ze = entries.nextElement();
                String[] parts = ze.getName().split("\\.");
                int i = Integer.getInteger(parts[0]);
                if (zipData[i] == null) {
                    zipData[i] = new ZipChunk();
                }
                if (parts[1].equals("data")) {
                    zipData[i].data = ze;
                } else if (parts[1].equals("resources")) {
                    zipData[i].resources = ze;
                }
            }
            for (int i = 0; i < REGION_SQUARE_SIZE; ++i) {
                if (zipData[i] != null) {
                    data[i] = new ChunkData(this);
                    data[i].loadFromDisk(zf, zipData[i]);
                }
            }
        }
    }

    public void writeRegion(Path file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
            try (ZipOutputStream zos = new ZipOutputStream(fos)) {
                for (int i = 0; i < REGION_SQUARE_SIZE; ++i) {
                    if (data[i] != null) {
                        data[i].writeToZip(zos, Integer.toString(i));
                    }
                }
            }
        }
    }


    public static int chunkPosToIndex(ChunkPos pos) {
        int px = pos.x % REGION_SIZE;
        int pz = pos.z % REGION_SIZE;
        if (px < 0) px += REGION_SIZE;
        if (pz < 0) pz += REGION_SIZE;
        return (px << 5) + pz;
    }

    public static int chunkPosToIndex(int x, int z) {
        int px = x % REGION_SIZE;
        int pz = z % REGION_SIZE;
        if (px < 0) px += REGION_SIZE;
        if (pz < 0) pz += REGION_SIZE;
        return (px << 5) + pz;
    }

    public static class ZipChunk {
        public ZipEntry data;
        public ZipEntry resources;
    }
}
