package xyz.wagyourtail.minimap.scanner;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ChunkData {
    public final MapRegion region;

    public final int[] heightmap = new int[256];
    public final int[] blockid = new int[256];
    public final int[] biomeid = new int[256];


    public final int[] oceanFloorHeightmap = new int[256];
    public final int[] oceanFloorBlockid = new int[256];
    public final int[] oceanFloorBiomeid = new int[256];

    public long updateTime;

    public List<ResourceLocation> resources = new ArrayList<>();

    public ChunkData(MapRegion region) {
        this.region = region;
    }

    public void loadFromChunk(ChunkAccess chunk, Level level) {
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
