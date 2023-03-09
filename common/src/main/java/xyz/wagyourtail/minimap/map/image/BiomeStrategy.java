package xyz.wagyourtail.minimap.map.image;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import xyz.wagyourtail.minimap.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.chunkdata.parts.SurfaceDataPart;

public class BiomeStrategy implements ImageStrategy {
    protected static final Minecraft minecraft = Minecraft.getInstance();


    //TODO: finish, fix, and test
    @Override
    public DynamicTexture load(ChunkLocation location, ChunkData data) {
        SurfaceDataPart surface = data.getData(SurfaceDataPart.class).orElse(null);
        if (surface == null) {
            return null;
        }
        assert minecraft.level != null;
        Registry<Biome> biomeRegistry = minecraft.level.registryAccess().registryOrThrow(Registries.BIOME);
        NativeImage image = new NativeImage(16, 16, false);
        for (int i = 0; i < 256; ++i) {
            int x = (i >> 4) % 16;
            int z = i % 16;
            image.setPixelRGBA(
                x,
                z,
                0x7FFFFFFF & colorFormatSwap(colorForBiome(data.getBiome(surface.biomeid[i]), biomeRegistry))
            );
        }
        return new DynamicTexture(image);
    }

    private int colorForBiome(ResourceLocation biome, Registry<Biome> registry) {
        //TODO finish
        return 0;
    }

}
