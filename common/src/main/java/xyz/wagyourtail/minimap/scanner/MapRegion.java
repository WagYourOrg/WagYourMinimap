package xyz.wagyourtail.minimap.scanner;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.List;

public class MapRegion {
    public final MapLevel parent;
    public final MapLevel.Pos position;

    public final ChunkData[] data = new ChunkData[256];

    public MapRegion(MapLevel parent, MapLevel.Pos pos) {
        this.parent = parent;
        this.position = pos;
    }

    public void loadChunkFromServer(ChunkAccess chunk, Level level) {
        ChunkPos pos = chunk.getPos();
        int px = pos.x % 16;
        int pz = pos.z % 16;
        if (px < 0) px += 16;
        if (pz < 0) pz += 16;
        int index = (px << 4) + pz;
        if (data[index] == null) data[index] = new ChunkData(this);
        data[index].loadFromChunk(chunk, level);
    }

    public void writeRegion() {

    }
}
