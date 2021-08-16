package xyz.wagyourtail.minimap.data;

import net.minecraft.world.level.ChunkPos;

public record ChunkLocation(MapLevel level, RegionPos region, int index) {
    public static final int REGION_SIZE = 32;

    public static record RegionPos(int x, int z) {
        public String getString() {
            return x + "," + z;
        }
    }

    public static ChunkLocation locationForChunkPos(MapLevel level, ChunkPos pos) {
        RegionPos rp = new RegionPos(pos.getRegionX(), pos.getRegionZ());
        return new ChunkLocation(level, rp, chunkPosToIndex(pos));
    }

    public static ChunkLocation locationForChunkPos(MapLevel level, int chunkX, int chunkZ) {
        RegionPos rp = new RegionPos(chunkX >> 5, chunkZ >> 5);
        return new ChunkLocation(level, rp, chunkPosToIndex(chunkX, chunkZ));
    }

    public static int chunkPosToIndex(ChunkPos pos) {
        int px = pos.x % REGION_SIZE;
        int pz = pos.z % REGION_SIZE;
        if (px < 0) px += REGION_SIZE;
        if (pz < 0) pz += REGION_SIZE;
        return (px << 5) + pz;
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
        return (region.x << 5) + xCord;
    }

    public int getChunkZ() {
        int zCord = index % REGION_SIZE;
        return (region.x << 5) + zCord;
    }

}
