package xyz.wagyourtail.minimap.map.image.colors;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AccurateBlockColors extends BlockColors {
    protected Map<BlockState, Integer> blockColorCache = new ConcurrentHashMap<>();
    protected Map<BsBiome, Integer> foliageColorCache = new ConcurrentHashMap<>();
    protected Map<BsBiome, Integer> grassColorCache = new ConcurrentHashMap<>();

    @Override
    public int getGrassColor(BlockState block, BlockPos pos, @Nullable Biome biome) {
        if (biome == null) {
            return block.getMapColor(Minecraft.getInstance().level, pos).col;
        }
        return grassColorCache.computeIfAbsent(new BsBiome(block, biome), (bsb) -> {
            int mask = getBlockColor(block, pos);
            int grass = biome.getGrassColor(0, 0);
            return (((grass >> 16) & 0xFF) * (mask >> 16 & 0xFF) / 255 << 16) |
                (((grass >> 8) & 0xFF) * (mask >> 8 & 0xFF) / 255 << 8) | ((grass & 0xFF) * (mask & 0xFF) / 255);
        });
    }

    @Override
    public int getLeavesColor(BlockState block, BlockPos pos, @Nullable Biome biome) {
        if (biome == null) {
            return block.getMapColor(Minecraft.getInstance().level, pos).col;
        }
        return foliageColorCache.computeIfAbsent(new BsBiome(block, biome), (bsb) -> {
            int mask = getBlockColor(block, pos);
            int leaves = biome.getFoliageColor();
            // slightly darken leaves
            int maskR = (int) (((mask >> 16) & 0xFF) * .9f);
            int maskG = (int) (((mask >> 8) & 0xFF) * .9f);
            int maskB = (int) ((mask & 0xFF) * .9f);
            return (((leaves >> 16) & 0xFF) * (maskR & 0xFF) / 255 << 16) |
                (((leaves >> 8) & 0xFF) * (maskG & 0xFF) / 255 << 8) | ((leaves & 0xFF) * maskB / 255);
        });
    }

    @Override
    public int getBlockColor(BlockState block, BlockPos pos) {
        return blockColorCache.computeIfAbsent(block, (bs) -> {
            NativeImage image = null;
            ResourceLocation blockLocation = Registry.BLOCK.getKey(bs.getBlock());
            try {
                // try for top texture
                ResourceLocation imageLocation = new ResourceLocation(
                    blockLocation.getNamespace(),
                    "textures/block/" + blockLocation.getPath() + "_top.png"
                );
                try (InputStream stream = minecraft.getResourceManager().getResource(imageLocation).getInputStream()) {
                    image = NativeImage.read(stream);
                } catch (IOException ignored) {

                    // no top texture, do default texture
                    imageLocation = new ResourceLocation(
                        blockLocation.getNamespace(),
                        "textures/block/" + blockLocation.getPath() + ".png"
                    );
                    try (
                        InputStream stream = minecraft.getResourceManager()
                            .getResource(imageLocation)
                            .getInputStream()
                    ) {
                        image = NativeImage.read(stream);
                    } catch (IOException ignored2) {
                        // no texture, fall back to material color
                        return block.getMapColor(Minecraft.getInstance().level, pos).col;
                    }
                }

                // compute average color
                long r = 0;
                long g = 0;
                long b = 0;
                int w = image.getWidth();
                int h = image.getHeight();
                long l = (long) w * h;
                for (int x = 0; x < w; x++) {
                    for (int y = 0; y < h; y++) {
                        // actually abgr pixel but mojang function name bad xd
                        int pixel = image.getPixelRGBA(x, y);
                        // ignore fully transparent pixels, so it's not super dark
                        if (pixel >> 24 == 0) {
                            --l;
                            continue;
                        }
                        b += (pixel >> 16) & 0xFF;
                        g += (pixel >> 8) & 0xFF;
                        r += pixel & 0xFF;
                    }
                }
                b /= l;
                g /= l;
                r /= l;
                return (((int) r) << 16) | (((int) g) << 8) | ((int) b);
            } finally {
                if (image != null) {
                    image.close();
                }
            }
        });
    }

    public record BsBiome(BlockState block, Biome biome) {
    }

}
