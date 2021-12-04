package xyz.wagyourtail.minimap.map.image.imager;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import xyz.wagyourtail.minimap.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.chunkdata.parts.SurfaceDataPart;
import xyz.wagyourtail.minimap.map.image.colors.IBlockColors;

public interface SurfaceImager extends IBlockColors {
    Minecraft minecraft = Minecraft.getInstance();

    @Override
    default DynamicTexture load(ChunkLocation location, ChunkData data) {
        SurfaceDataPart surface = data.getData(SurfaceDataPart.class).orElse(null);
        if (surface == null) {
            return null;
        }
        NativeImage image = new NativeImage(16, 16, false);
        assert minecraft.level != null;
        Registry<Biome> biomeRegistry = minecraft.level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
        int[] north = data.north()
            .get()
            .getData(SurfaceDataPart.class)
            .map(e -> e.heightmap)
            .orElseGet(() -> new int[256]);
        int[] south = data.south()
            .get()
            .getData(SurfaceDataPart.class)
            .map(e -> e.heightmap)
            .orElseGet(() -> new int[256]);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        int i;
        int chunkX = location.getChunkX() << 4;
        int chunkZ = location.getChunkZ() << 4;
        for (i = 0; i < 256; ++i) {
            int x = (i >> 4) % 16;
            int z = i % 16;
            pos.set(chunkX | x, surface.heightmap[i], chunkZ | z);
            Biome biome = biomeRegistry.get(data.getBiome(surface.biomeid[i]));
            BlockState state = data.getBlockState(surface.blockid[i]);
            int color;
            if (isWater(state.getBlock())) {
                int waterColor = getWaterColor(state, pos, biome);
                state = data.getBlockState(surface.oceanFloorBlockid[i]);
                float waterRatio = Math.min(
                    // 0.7 - 1.0 depending on depth of water 1 - 10 blocks...
                    .7f + .3F * (surface.heightmap[i] - surface.oceanFloorHeightmap[i]) / 10F, 1.0F);
                color = colorCombine(waterColor, getBlockColor(state, pos), waterRatio);
            } else if (isGrass(state.getBlock())) {
                color = getGrassColor(state, pos, biome);
            } else if (isLeaves(state.getBlock())) {
                color = getLeavesColor(state, pos, biome);
            } else {
                color = getBlockColor(state, pos);
            }
            if (z == 0) {
                color = brightnessForHeight2(color, surface.heightmap[i], north[15 + 16 * x], surface.heightmap[i + 1]);
            } else if (z == 15) {
                color = brightnessForHeight2(color, surface.heightmap[i], surface.heightmap[i - 1], south[16 * x]);
            } else {
                color = brightnessForHeight2(
                    color,
                    surface.heightmap[i],
                    surface.heightmap[i - 1],
                    surface.heightmap[i + 1]
                );
            }
            image.setPixelRGBA(x, z, 0xFF000000 | colorFormatSwap(color));
        }
        return new DynamicTexture(image);
    }


}
