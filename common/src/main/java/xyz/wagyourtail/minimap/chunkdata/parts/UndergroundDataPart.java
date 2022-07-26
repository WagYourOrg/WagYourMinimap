package xyz.wagyourtail.minimap.chunkdata.parts;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import xyz.wagyourtail.minimap.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.chunkdata.updater.UndergroundDataUpdater;
import xyz.wagyourtail.minimap.map.MapServer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UndergroundDataPart extends DataPart<UndergroundDataPart> {
    private static final ResourceLocation plains = new ResourceLocation("minecraft", "plains");
    // powers of 2 are probably best
    public final int sectionHeight;
    public final List<BlockState> blocks = new ArrayList<>();
    public final List<ResourceLocation> biomes = new ArrayList<>();
    public final Data[] data;

    /**
     * @param parent container
     */
    public UndergroundDataPart(ChunkData parent) {
        super(parent);
        MapServer.MapLevel level = parent.location.level();
        this.sectionHeight = UndergroundDataUpdater.sectionHeight;
        data = new Data[(level.maxHeight() - level.minHeight()) / sectionHeight + 1];
    }

    @Override
    public int getDataVersion() {
        return 0;
    }

    @Override
    public boolean mergeFrom(UndergroundDataPart other) {
        // unused
        return false;
    }

    @Override
    public void deserialize(ByteBuffer buffer, int size) {
        // unused
    }

    @Override
    public void serialize(ByteBuffer buffer) {
        // unused
    }

    @Override
    public int getBytes() {
        return 0;
    }

    @Override
    public void usedBlockStates(Set<Integer> used) {
        // unused
    }

    @Override
    public void remapBlockStates(Map<Integer, Integer> map) {
        // unused
    }

    @Override
    public void usedBiomes(Set<Integer> used) {
        // unused
    }

    @Override
    public void remapBiomes(Map<Integer, Integer> map) {
        // unused
    }

    public int getOrRegisterBlockState(BlockState state) {
        if (state == null) {
            return 0;
        }
        for (int j = 0; j < blocks.size(); ++j) {
            if (state.equals(blocks.get(j))) {
                return j + 1;
            }
        }
        blocks.add(state);
        return blocks.size();
    }

    public BlockState getBlockState(int i) {
        if (i < 1 || i > blocks.size()) {
            return Blocks.AIR.defaultBlockState();
        }
        return blocks.get(i - 1);
    }

    public int getOrRegisterBiome(ResourceLocation biome) {
        for (int i = 0; i < biomes.size(); ++i) {
            if (biomes.get(i).equals(biome)) {
                return i + 1;
            }
        }
        biomes.add(biome);
        return biomes.size();
    }

    public ResourceLocation getBiome(int i) {
        if (i < 1 || i > biomes.size()) {
            return plains;
        }
        return biomes.get(i - 1);
    }

    public static record Data(int[] blockid, int[] heightmap, byte[] lightmap, int[] biomeid) {
    }

}
