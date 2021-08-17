package xyz.wagyourtail.minimap.client.gui.image;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.client.gui.ThreadsafeDynamicTexture;

public class BiomeStrategy extends AbstractImageStrategy {
    @Override
    public ThreadsafeDynamicTexture load(ChunkLocation location, ChunkData data) {
        assert minecraft.level != null;
        Registry<Biome> biomeRegistry = minecraft.level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
        NativeImage image = new NativeImage(16, 16, false);
        for (int i = 0; i < 256; ++i) {
            int x = (i >> 4) % 16;
            int z = i % 16;
            image.setPixelRGBA(x, z, 0x7FFFFFFF & colorFormatSwap(colorForBiome(data.getResourceLocation(data.biomeid[i]), biomeRegistry)));
        }
        return new ThreadsafeDynamicTexture(image);
    }

    private int colorForBiome(ResourceLocation biome, Registry<Biome> registry) {
        //TODO finish
        return 0;
    }

}
