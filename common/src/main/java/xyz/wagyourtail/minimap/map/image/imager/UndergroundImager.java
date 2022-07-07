package xyz.wagyourtail.minimap.map.image.imager;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.chunkdata.parts.UndergroundDataPart;
import xyz.wagyourtail.minimap.chunkdata.updater.UndergroundDataUpdater;
import xyz.wagyourtail.minimap.map.image.colors.IBlockColors;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public interface UndergroundImager extends IBlockColors {
    Minecraft minecraft = Minecraft.getInstance();
    AtomicInteger lastY = new AtomicInteger(0);

    @Override
    default String getDerivitiveKey() {
        lastY.set(Mth.clamp(
            (minecraft.cameraEntity.getBlockY() - minecraft.level.dimensionType().minY()) /
                UndergroundDataUpdater.sectionHeight,
            0,
            minecraft.level.dimensionType().height() / UndergroundDataUpdater.sectionHeight
        ));
        return IBlockColors.super.getDerivitiveKey() + "$" + lastY;
    }

    @Override
    default DynamicTexture load(ChunkLocation location, ChunkData data) {
        Level level = minecraft.level;
        if (level == null || minecraft.cameraEntity == null) {
            return null;
        }
        int y = lastY.get();
        Optional<UndergroundDataPart> ud = data.getData(UndergroundDataPart.class);
        if (ud.isEmpty() || ud.get().data[y] == null) {
            MinimapApi.getInstance().getChunkUpdateStrategy(UndergroundDataUpdater.class).scan(
                level,
                location,
                y
            );
            return null;
        } else {
            UndergroundDataPart udPart = ud.get();

            UndergroundDataPart.Data mapData = udPart.data[y];
            int[] heightmap = mapData.heightmap();
            int[] blockid = mapData.blockid();
            int[] biomeid = mapData.biomeid();

            NativeImage image = new NativeImage(16, 16, false);

            Registry<Biome> biomeRegistry = minecraft.level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);

            int[] north = data.north()
                .get()
                .getData(UndergroundDataPart.class)
                .map(e -> e.data[y])
                .map(UndergroundDataPart.Data::heightmap)
                .orElseGet(() -> new int[256]);
            int[] south = data.south()
                .get()
                .getData(UndergroundDataPart.class)
                .map(e -> e.data[y])
                .map(UndergroundDataPart.Data::heightmap)
                .orElseGet(() -> new int[256]);

            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            int i;
            int chunkX = location.getChunkX() << 4;
            int chunkZ = location.getChunkZ() << 4;

            for (i = 0; i < 256; ++i) {
                int x = (i >> 4) % 16;
                int z = i % 16;
                pos.set(chunkX | x, heightmap[i], chunkZ | z);
                Biome biome = biomeRegistry.get(udPart.getBiome(biomeid[i]));
                BlockState state = udPart.getBlockState(blockid[i]);
                int color;
                if (isWater(state.getBlock())) {
                    color = getWaterColor(state, pos, biome);
                } else if (isGrass(state.getBlock())) {
                    color = getGrassColor(state, pos, biome);
                } else if (isLeaves(state.getBlock())) {
                    color = getLeavesColor(state, pos, biome);
                } else {
                    color = getBlockColor(state, pos);
                }
                if (z == 0) {
                    color = brightnessForHeight2(color, heightmap[i], north[15 + 16 * x], heightmap[i + 1]);
                } else if (z == 15) {
                    color = brightnessForHeight2(color, heightmap[i], heightmap[i - 1], south[16 * x]);
                } else {
                    color = brightnessForHeight2(
                        color,
                        heightmap[i],
                        heightmap[i - 1],
                        heightmap[i + 1]
                    );
                }
                image.setPixelRGBA(x, z, 0xFF000000 | colorFormatSwap(color));
            }
            return new DynamicTexture(image);
        }
    }

}
