package xyz.wagyourtail.minimap.scanner;

import com.google.common.cache.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import xyz.wagyourtail.minimap.WagYourMinimap;

import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class MapLevel {
    public final String server_slug;
    public final String level_slug;
    public final Path mapCacheLocation;
    public LoadingCache<Pos, MapRegion> regionCache;

    public final RegionLoader loader = new RegionLoader();

    public MapLevel(String server_slug, String level_slug) {
        this.server_slug = server_slug;
        this.level_slug = level_slug;
        mapCacheLocation = WagYourMinimap.configFolder.resolve(server_slug + "/" + level_slug);
        resizeCache(WagYourMinimap.INSTANCE.config.regionCacheSize);
    }

    public void onRegionRemoved(RemovalNotification<Pos, MapRegion> notification) {
        notification.getValue().writeRegion();
    }

    public void resizeCache(long newCacheSize) {
        CacheBuilder<Pos, MapRegion> builder = (CacheBuilder) CacheBuilder.newBuilder();
        builder
            .maximumSize(newCacheSize)
            .removalListener(this::onRegionRemoved);

        LoadingCache<Pos, MapRegion> oldCache = regionCache;
        regionCache = builder.build(new RegionLoader());
        if (oldCache != null) {
            regionCache.putAll(oldCache.asMap());
        }
    }

    public void onServerChunk(ChunkAccess chunk, Level level) throws ExecutionException {
        ChunkPos pos = chunk.getPos();
        regionCache.get(new Pos(pos.getRegionX(), pos.getRegionZ())).loadChunkFromServer(chunk, level);
    }

    public static record Pos(int x, int z) {
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Pos pos)) return false;
            return x == pos.x && z == pos.z;
        }

        public int hashCode() {
            return Objects.hash(x, z);
        }
    }

    public class RegionLoader extends CacheLoader<Pos, MapRegion> {

        @Override
        public MapRegion load(Pos key) {
            //TODO: read from file, or new
            return new MapRegion(MapLevel.this, key);
        }

    }

}
