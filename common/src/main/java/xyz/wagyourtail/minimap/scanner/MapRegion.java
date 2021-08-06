package xyz.wagyourtail.minimap.scanner;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.List;

public class MapRegion {
    public final MapLevel parent;
    public final MapLevel.Pos position;

    public List<ResourceLocation> resources = new ArrayList<>();

    public final ChunkData[] data = new ChunkData[256];

    public MapRegion(MapLevel parent, MapLevel.Pos pos) {
        this.parent = parent;
        this.position = pos;
    }

    public void parseChunkFromServer(ChunkAccess chunk) {
        ChunkPos pos = chunk.getPos();
        int px = pos.x % 16;
        int pz = pos.z % 16;
        if (px < 0) px += 16;
        if (pz < 0) pz += 16;
        int index = (px << 4) + pz;
        ChunkData cdata = data[index] = new ChunkData();
        for (int i = 0; i < 256; ++i) {
            int x = (i >> 4) % 16;
            int z = i % 16;

            cdata.blockid[i] = getOrRegisterResourceLocation(Registry.BLOCK.getKey(chunk.getBlockState(new BlockPos(x, cdata.heightmap[i] = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z), z)).getBlock()));
            //TODO: figure out why broken, do I need to get the biome some different way?
            cdata.biomeid[i] = getOrRegisterResourceLocation(BuiltinRegistries.BIOME.getKey(chunk.getBiomes().getNoiseBiome(x, cdata.heightmap[i], z)));

            cdata.oceanFloorBlockid[i] = getOrRegisterResourceLocation(Registry.BLOCK.getKey(chunk.getBlockState(new BlockPos(x, cdata.oceanFloorHeightmap[i] = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z), z)).getBlock()));
            //TODO: figure out why broken, do I need to get the biome some different way?
            cdata.oceanFloorBiomeid[i] = getOrRegisterResourceLocation(BuiltinRegistries.BIOME.getKey(chunk.getBiomes().getNoiseBiome(x, cdata.oceanFloorHeightmap[i], z)));
        }
    }

    private int getOrRegisterResourceLocation(ResourceLocation id) {
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

    public void writeRegion() {

    }
}
