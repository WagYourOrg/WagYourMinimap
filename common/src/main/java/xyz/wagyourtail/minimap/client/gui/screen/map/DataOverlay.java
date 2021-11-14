package xyz.wagyourtail.minimap.client.gui.screen.map;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.Registry;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.chunkdata.parts.SurfaceDataPart;
import xyz.wagyourtail.minimap.map.MapServer;

public class DataOverlay extends AbstractFullscreenOverlay {
    public DataOverlay(ScreenMapRenderer parent) {
        super(parent);
    }

    @Override
    public void renderOverlay(PoseStack stack, int mouseX, int mouseY) {
        int x = (int) (parent.topX + parent.xDiam * mouseX / parent.width);
        int z = (int) (parent.topZ + parent.zDiam * mouseY / parent.height);

        MapServer.MapLevel level = MinimapApi.getInstance().getMapServer().getCurrentLevel();
        ChunkData chunk = ChunkLocation.locationForChunkPos(level, x >> 4, z >> 4).get();
        int y = 0;
        String biome = "unknown";
        String block = "unknown";
        byte light = 0;
        if (chunk != null) {
            SurfaceDataPart surface = chunk.getData(SurfaceDataPart.class).orElse(null);
            if (surface != null) {
                y = surface.heightmap[SurfaceDataPart.blockPosToIndex(x, z)];
                block = Registry.BLOCK.getKey(chunk.getBlockState(surface.blockid[SurfaceDataPart.blockPosToIndex(x,
                    z
                )]).getBlock()).toString();
                biome = chunk.getBiome(surface.biomeid[SurfaceDataPart.blockPosToIndex(x, z)]).toString();
                light = surface.blocklight[SurfaceDataPart.blockPosToIndex(x, z)];
            }
        }
        minecraft.font.draw(stack, level.parent().server_slug, 50, 4, 0xFFFFFF);
        minecraft.font.draw(stack,
            String.format("%d, %d, %d  %s/%s %d", x, y, z, biome, block, light),
            50,
            parent.height - 10,
            0xFFFFFF
        );
    }

}
