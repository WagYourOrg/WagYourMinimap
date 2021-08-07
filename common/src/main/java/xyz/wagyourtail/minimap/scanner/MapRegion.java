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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class MapRegion {
    public final MapLevel parent;
    public final MapLevel.Pos position;

    public final ChunkData[] data = new ChunkData[256];

    public MapRegion(MapLevel parent, MapLevel.Pos pos) {
        this.parent = parent;
        this.position = pos;
    }

    public void loadChunkFromServer(ChunkAccess chunk, Level level) {
        ChunkPos pos = chunk.getPos();
        int px = pos.x % 16;
        int pz = pos.z % 16;
        if (px < 0) px += 16;
        if (pz < 0) pz += 16;
        int index = (px << 4) + pz;
        if (data[index] == null) data[index] = new ChunkData(this);
        data[index].loadFromChunk(chunk, level);
    }

    public void readRegion(Path file) throws IOException {
        ZipChunk[] zipData = new ZipChunk[256];
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
            for (int i = 0; i < 256; ++i) {
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
                for (int i = 0; i < 256; ++i) {
                    if (data[i] != null) {
                        data[i].writeToZip(zos, Integer.toString(i));
                    }
                }
            }
        }
    }

    public static class ZipChunk {
        public ZipEntry data;
        public ZipEntry resources;
    }
}
