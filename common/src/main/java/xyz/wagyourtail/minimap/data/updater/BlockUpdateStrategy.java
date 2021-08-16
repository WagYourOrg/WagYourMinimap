package xyz.wagyourtail.minimap.data.updater;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import xyz.wagyourtail.minimap.data.ChunkData;
import xyz.wagyourtail.minimap.data.ChunkLocation;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class BlockUpdateStrategy extends AbstractChunkUpdateStrategy {
    public static final Event<BlockUpdate> BLOCK_UPDATE_EVENT = EventFactory.createLoop();

    private final LoadingCache<BlockUpdateData, Runnable> updateCache = CacheBuilder.newBuilder().expireAfterWrite(5000, TimeUnit.MILLISECONDS).removalListener((a) -> ((Runnable)a.getValue()).run()).build(new CacheLoader<>() {
        @Override
        public Runnable load(BlockUpdateData key) {
            return () -> {
                updateChunk(key.location, (loc, oldData) -> ChunkLoadStrategy.loadFromChunk(key.chunk, key.level, oldData));
                updateNeighborLighting(key.level, getBlockLightLayer(key.level), key.location.getRegionX(), key.location.getRegionZ());
            };
        }
    });

    public BlockUpdateStrategy() {
        super(3);
    }

    public void updateNeighborLighting(Level level, LayerLightEventListener light, int chunkX, int chunkZ) {
        for (int i = chunkX - 1; i < chunkX + 2; ++i) {
            for (int j = chunkZ - 1; j < chunkZ + 2; ++j) {
                if (level.hasChunk(i, j)) {
                    int finalI = i;
                    int finalJ = j;
                    ChunkAccess chunk = level.getChunk(finalI, finalJ);
                    if (chunk == null) continue;
                    updateChunk(
                        getChunkLocation(level, finalI, finalJ),
                        (region, chunkData) -> updateLighting(level, chunk, light, chunkData, finalI, finalJ)
                    );
                }
            }
        }
    }

    public ChunkData updateLighting(Level level, ChunkAccess chunk, LayerLightEventListener light, ChunkData oldData, int chunkX, int chunkZ) {
        if (oldData == null) return null;
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(0, 0, 0);
        if (chunk != null) {
            boolean invalidate = false;
            for (int i = 0; i < 256; ++i) {
                int x = (i >> 4) % 16;
                int z = i % 16;
                byte newBlockLight = (byte) light.getLightValue(blockPos.set((chunkX << 4) + x, oldData.heightmap[i] + 1, (chunkZ << 4) + z));
                invalidate = invalidate || newBlockLight != oldData.blocklight[i];
                oldData.blocklight[i] = newBlockLight;
            }
            if (invalidate) oldData.markDirty();
        }
        return oldData;
    }

    @Override
    protected void registerEventListener() {
        BLOCK_UPDATE_EVENT.register((pos, level) -> {
            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;
            ChunkAccess chunk = level.getChunk(chunkX, chunkZ);
            try {
                updateCache.get(new BlockUpdateData(level, chunk, getChunkLocation(level, chunkX, chunkZ)));
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    private record BlockUpdateData(Level level, ChunkAccess chunk, ChunkLocation location) {}

    public interface BlockUpdate {
        void onBlockUpdate(BlockPos pos, Level level);
    }
}
