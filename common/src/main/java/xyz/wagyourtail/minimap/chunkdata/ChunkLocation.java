package xyz.wagyourtail.minimap.chunkdata;

import net.minecraft.world.level.ChunkPos;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.map.MapServer;

public record ChunkLocation(MapServer.MapLevel level, int regionX, int regionZ, int index) {
    public static final int REGION_SIZE = 32;

    public static ChunkLocation locationForChunkPos(MapServer.MapLevel level, ChunkPos pos) {
        return new ChunkLocation(level, pos.getRegionX(), pos.getRegionZ(), chunkPosToIndex(pos));
    }

    public static int chunkPosToIndex(ChunkPos pos) {
        int px = pos.x % REGION_SIZE;
        int pz = pos.z % REGION_SIZE;
        if (px < 0) px += REGION_SIZE;
        if (pz < 0) pz += REGION_SIZE;
        return (px << 5) + pz;
    }

    public static ChunkLocation locationForChunkPos(MapServer.MapLevel level, int chunkX, int chunkZ) {
        return new ChunkLocation(level,chunkX >> 5, chunkZ >> 5, chunkPosToIndex(chunkX, chunkZ));
    }

    public static int chunkPosToIndex(int x, int z) {
        int px = x % REGION_SIZE;
        int pz = z % REGION_SIZE;
        if (px < 0) px += REGION_SIZE;
        if (pz < 0) pz += REGION_SIZE;
        return (px << 5) + pz;
    }

    public int getChunkX() {
        int xCord = index >> 5;
        return (regionX << 5) + xCord;
    }

    public int getChunkZ() {
        int zCord = index % REGION_SIZE;
        return (regionZ << 5) + zCord;
    }

    public ChunkData get() {
        return MinimapApi.getInstance().cacheManager.loadChunk(this);
    }

    public String getRegionSlug() {
        return regionX + "," + regionZ;
    }

    @Override
    public String toString() {
        return getRegionSlug() + ":" + index;
    }

}
