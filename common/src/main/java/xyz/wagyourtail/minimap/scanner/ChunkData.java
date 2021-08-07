package xyz.wagyourtail.minimap.scanner;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ChunkData {
    public final MapRegion parent;
    public long updateTime;

    public final int[] heightmap = new int[256];
    public final int[] blockid = new int[256];
    public final int[] biomeid = new int[256];


    public final int[] oceanFloorHeightmap = new int[256];
    public final int[] oceanFloorBlockid = new int[256];
    public final int[] oceanFloorBiomeid = new int[256];


    public final List<ResourceLocation> resources = new ArrayList<>();

    public ChunkData(MapRegion parent) {
        this.parent = parent;
    }

    public synchronized void loadFromChunk(ChunkAccess chunk, Level level) {
        CompletableFuture.runAsync(() -> {
            updateTime = System.currentTimeMillis();
            ChunkPos pos = chunk.getPos();
            BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
            for (int i = 0; i < 256; ++i) {
                int x = (i >> 4) % 16;
                int z = i % 16;

                Registry<Biome> biomeRegistry = level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);

                this.heightmap[i] = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                this.blockid[i] = getOrRegisterResourceLocation(Registry.BLOCK.getKey(chunk.getBlockState(blockPos.set(pos.x << 4 + x, this.heightmap[i], pos.z << 4)).getBlock()));
                this.biomeid[i] = getOrRegisterResourceLocation(biomeRegistry.getKey(level.getBiome(blockPos)));

                this.oceanFloorHeightmap[i] = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
                this.oceanFloorBlockid[i] = getOrRegisterResourceLocation(Registry.BLOCK.getKey(chunk.getBlockState(blockPos.setY(this.oceanFloorHeightmap[i])).getBlock()));
                this.oceanFloorBiomeid[i] = getOrRegisterResourceLocation(biomeRegistry.getKey(level.getBiome(blockPos)));
            }
        });
    }
    
    public synchronized void loadFromDisk(ZipFile file, MapRegion.ZipChunk chunk) throws IOException {
        try (InputStream stream = file.getInputStream(chunk.data)) {
            ByteBuffer data = ByteBuffer.wrap(stream.readAllBytes());
            data.rewind();
            this.updateTime = data.getLong();
            for (int i = 0; i < 256; ++i) {
                heightmap[i] = data.getInt();
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
        }
        try (InputStream stream = file.getInputStream(chunk.resources)) {
            for (String resource : new String(stream.readAllBytes()).split("\n")) {
                resources.add(new ResourceLocation(resource));
            }
        }
    }

    public synchronized void writeToZip(ZipOutputStream out, String pos_slug) throws IOException {
        ByteBuffer data = ByteBuffer.allocate(Long.BYTES + Integer.BYTES * 256 * 6);
        data.putLong(updateTime);
        for (int i = 0; i < 256; ++i) {
            data.putInt(heightmap[i]);
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
        out.write(data.array());
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

}
