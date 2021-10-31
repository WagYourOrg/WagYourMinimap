package xyz.wagyourtail.minimap.map.chunkdata.updater;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class BlockUpdateStrategy extends AbstractChunkUpdateStrategy {
    public static final Event<BlockUpdate> BLOCK_UPDATE_EVENT = EventFactory.createLoop();

    private final LoadingCache<BlockUpdateData, Runnable> updateCache = CacheBuilder.newBuilder().expireAfterWrite(5000, TimeUnit.MILLISECONDS).removalListener((a) -> ((Runnable) a.getValue()).run()).build(new CacheLoader<>() {
        @Override
        public Runnable load(BlockUpdateData key) {
            return () -> updateNeighborLighting(key.level, key.chunkX, key.chunkZ);
        }
    });

    public BlockUpdateStrategy() {
        super();
    }

    public void updateNeighborLighting(Level level, int chunkX, int chunkZ) {
        for (int i = chunkX - 1; i < chunkX + 2; ++i) {
            for (int j = chunkZ - 1; j < chunkZ + 2; ++j) {
                if (level.hasChunk(i, j)) {
                    ChunkAccess chunk = level.getChunk(i, j, ChunkStatus.FULL, false);
                    if (chunk == null) continue;
                    //TODO: update lighting only function
                    updateChunk(getChunkLocation(level, i, j), (location, oldData) -> ChunkLoadStrategy.loadFromChunk(location, chunk, level, oldData));
                }
            }
        }
    }

    @Override
    protected void registerEventListener() {
        BLOCK_UPDATE_EVENT.register((pos, level) -> {
            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;
            try {
                updateCache.get(new BlockUpdateData(level, chunkX, chunkZ));
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    public interface BlockUpdate {
        void onBlockUpdate(BlockPos pos, Level level);

    }

    private record BlockUpdateData(Level level, int chunkX, int chunkZ) {
    }

}
