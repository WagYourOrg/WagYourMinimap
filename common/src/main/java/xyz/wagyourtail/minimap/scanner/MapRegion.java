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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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
    public final CompletableFuture<ChunkData>[] data = new CompletableFuture[REGION_SQUARE_SIZE];

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
                try {
                    int i = Integer.parseInt(parts[0]);
                    if (zipData[i] == null) {
                        zipData[i] = new ZipChunk();
                    }
                    if (parts[1].equals("data")) {
                        zipData[i].data = ze;
                    } else if (parts[1].equals("resources")) {
                        zipData[i].resources = ze;
                    } else {
                        System.err.println("bad zip entry: " + ze.getName());
                    }
                } catch (NumberFormatException ex) {
                    System.err.println("bad zip entry: " + ze.getName());
                }
            }
            for (int i = 0; i < REGION_SQUARE_SIZE; ++i) {
                if (zipData[i] != null) {
                    synchronized (data) {
                        int index = i;
                        data[i] = CompletableFuture.completedFuture(new ChunkData(this)).thenApply(u -> u.loadFromDisk(zf, zipData[index]));
                    }
                }
            }
        }
    }

    public void writeRegion(Path file) throws IOException {
        Path tempFile = file.getParent().resolve(file.getName(file.getNameCount() - 1) + ".tmp");
        try (FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {
            try (ZipOutputStream zos = new ZipOutputStream(fos)) {
                for (int i = 0; i < REGION_SQUARE_SIZE; ++i) {
                    if (data[i] != null) {
                        data[i].get().writeToZip(zos, Integer.toString(i));
                    }
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        Files.move(tempFile, file, StandardCopyOption.REPLACE_EXISTING);
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
