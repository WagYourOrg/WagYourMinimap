package xyz.wagyourtail.minimap.chunkdata.updater;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import xyz.wagyourtail.minimap.api.MinimapEvents;
import xyz.wagyourtail.minimap.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.chunkdata.cache.CacheManager;
import xyz.wagyourtail.minimap.chunkdata.parts.DataPart;
import xyz.wagyourtail.minimap.map.MapServer;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class AbstractChunkDataUpdater<T extends DataPart<T>> implements ChunkLoadEvent, BlockUpdateEvent, LightLevelSetEvent {
    private static final ThreadPoolExecutor data_pool = new ThreadPoolExecutor(
        4,
        4,
        0L,
        TimeUnit.NANOSECONDS,
        new LinkedBlockingQueue<>()
    );
    public static final Event<ChunkLoadEvent> CHUNK_LOAD = EventFactory.createLoop();
    public static final Event<BlockUpdateEvent> BLOCK_UPDATE = EventFactory.createLoop();
    public static final Event<LightLevelSetEvent> LIGHT_LEVEL = EventFactory.createLoop();

    public final Set<String> derivitivesToInvalidate = new HashSet<>();

    public AbstractChunkDataUpdater(Set<String> derivitivesToInvalidate) {
        registerEventListener();
        this.derivitivesToInvalidate.addAll(derivitivesToInvalidate);
    }

    protected void registerEventListener() {
        CHUNK_LOAD.register(this);
        BLOCK_UPDATE.register(this);
        LIGHT_LEVEL.register(this);
    }

    protected static LayerLightEventListener getBlockLightLayer(Level level) {
        return level.getLightEngine().getLayerListener(LightLayer.BLOCK);
    }

    protected void updateChunk(ChunkLocation location, ChunkUpdateListener<T> newChunkDataCreator) {
        //        synchronized (location.level()) {
        data_pool.execute(() -> {
            ChunkData chunkData = location.get();
            synchronized (chunkData) {
                chunkData.computeData(
                    getType(),
                    (old) -> newChunkDataCreator.onChunkUpdate(location, chunkData, old)
                );
                MinimapEvents.CHUNK_UPDATED.invoker().onChunkUpdate(
                    location,
                    chunkData,
                    this.getClass(),
                    getType()
                );
            }
        });
        //        }
    }

    public abstract Class<T> getType();

    protected ChunkLocation getChunkLocation(MapServer.MapLevel level, int chunkX, int chunkZ) {
        return ChunkLocation.locationForChunkPos(level, chunkX, chunkZ);
    }

    protected ChunkLocation getChunkLocation(MapServer.MapLevel level, ChunkPos pos) {
        return ChunkLocation.locationForChunkPos(level, pos);
    }

    @Override
    public void onBlockUpdate(BlockPos pos, Level level) {}

    @Override
    public void onLoadChunk(ChunkAccess chunk, Level level) {}

    @Override
    public void onLightLevel(ChunkSource chunkGetter, SectionPos pos) {}

    public interface ChunkUpdateListener<T extends DataPart<T>> {

        /*
         * NOT ALLOWED TO LOCK/GET ANOTHER CHUNK OR CacheManager WITHIN THIS FUNCTION!!!! - can deadlock
         * https://github.com/wagyourtail/WagYourMinimap/issues/7
         */
        T onChunkUpdate(ChunkLocation location, ChunkData data, T oldData);

    }

}
