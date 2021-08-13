package xyz.wagyourtail.minimap.scanner;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import xyz.wagyourtail.LazyResolver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ChunkData implements AutoCloseable {
    public final MapRegion parent;
    private Map<String, Derivitive<?>> derrivitives = new HashMap<>();
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
                data.put(blocklight[i]);
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

    public synchronized <T> LazyResolver<T> computeDerivitive(String key, Supplier<T> supplier) {
        //chunk is closed???
        if (derrivitives == null) {
            new Error().printStackTrace();
            return null;
        }
        Derivitive<T> der = (Derivitive<T>) derrivitives.computeIfAbsent(key, (k) -> new Derivitive<>(false, new LazyResolver<>(supplier)));
        if (der.old) derrivitives.put(key, new Derivitive<>(false, der.contained.then(old -> supplier.get(), true)));
        return der.contained;
    }

    Error firstClose = null;

    @Override
    public synchronized void close() {
        //double close????
        if (derrivitives == null) {
            new Error().printStackTrace();
            firstClose.printStackTrace();
            return;
        }
        firstClose = new Error();
        derrivitives.forEach((k,v) -> {
            if (v.contained instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) v.contained).close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        derrivitives = null;
    }

    public void copyDerivatives(ChunkData old) {
        this.derrivitives.putAll(old.getDerivativesAsOldAndClear());
    }

    private synchronized Map<String, Derivitive<?>> getDerivativesAsOldAndClear() {
        if (derrivitives == null) return ImmutableMap.of();
        Map<String, Derivitive<?>> derivitiveMap = ImmutableMap.copyOf(derrivitives);
        derivitiveMap.forEach((k,v) -> v.old = true);
        derrivitives.clear();
        return derivitiveMap;
    }

    public synchronized void invalidateDerivitives() {
        if (derrivitives == null) return;
        derrivitives.values().forEach((v) -> v.old = true);
    }

    public static int blockPosToIndex(BlockPos pos) {
        int x = pos.getX() % 16;
        int z = pos.getZ() % 16;
        if (x < 0) x += 16;
        if (z < 0) z += 16;
        return (x << 4) + z;
    }

    public static class Derivitive<T>{
        public boolean old;
        public final LazyResolver<T> contained;
        Derivitive(boolean old, LazyResolver<T> contained) {
            this.old = old;
            this.contained = contained;
        }
    }

}
