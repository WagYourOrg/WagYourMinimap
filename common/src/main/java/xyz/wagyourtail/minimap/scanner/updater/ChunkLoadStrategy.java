package xyz.wagyourtail.minimap.scanner.updater;

import dev.architectury.event.events.common.ChunkEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;
import xyz.wagyourtail.minimap.WagYourMinimap;
import xyz.wagyourtail.minimap.scanner.ChunkData;
import xyz.wagyourtail.minimap.scanner.MapLevel;
import xyz.wagyourtail.minimap.scanner.MapRegion;

public class ChunkLoadStrategy extends AbstractChunkUpdateStrategy {

    public synchronized ChunkData loadFromChunk(ChunkAccess chunk, Level level, MapRegion parent) {
        ChunkData data = new ChunkData(parent);
        data.updateTime = System.currentTimeMillis();
        ChunkPos pos = chunk.getPos();
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        Registry<Biome> biomeRegistry = level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
        for (int i = 0; i < 256; ++i) {
            int x = (i >> 4) % 16;
            int z = i % 16;


            data.heightmap[i] = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
            data.blockid[i] = data.getOrRegisterResourceLocation(Registry.BLOCK.getKey(chunk.getBlockState(blockPos.set((pos.x << 4) + x, data.heightmap[i], (pos.z << 4) + z)).getBlock()));
            data.biomeid[i] = data.getOrRegisterResourceLocation(biomeRegistry.getKey(level.getBiome(blockPos)));

            data.oceanFloorHeightmap[i] = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
            data.oceanFloorBlockid[i] = data.getOrRegisterResourceLocation(Registry.BLOCK.getKey(chunk.getBlockState(blockPos.setY(data.oceanFloorHeightmap[i])).getBlock()));
            data.oceanFloorBiomeid[i] = data.getOrRegisterResourceLocation(biomeRegistry.getKey(level.getBiome(blockPos)));
        }
        return data;
    }

    @Override
    protected void registerEventListener() {
        ChunkEvent.LOAD_DATA.register((ChunkAccess chunk, @Nullable ServerLevel level, CompoundTag nbt) -> {
            String server_slug = WagYourMinimap.INSTANCE.getServerName();
            String level_slug = WagYourMinimap.INSTANCE.getLevelName(level);
            ChunkPos pos = chunk.getPos();
            int index = MapRegion.chunkPosToIndex(pos);
            MapLevel.Pos region_pos = new MapLevel.Pos(pos.getRegionX(), pos.getRegionZ());
            updateChunk(server_slug,
                level_slug,
                level,
                region_pos,
                index,
                (region, oldData) -> loadFromChunk(chunk, WagYourMinimap.INSTANCE.resolveServerLevel(level), region),
                true
            );
        });
    }

}
