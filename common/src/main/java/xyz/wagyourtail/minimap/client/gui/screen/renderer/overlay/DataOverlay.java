package xyz.wagyourtail.minimap.client.gui.screen.renderer.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.client.gui.screen.renderer.AbstractFullscreenOverlay;
import xyz.wagyourtail.minimap.client.gui.screen.renderer.ScreenMapRenderer;
import xyz.wagyourtail.minimap.map.MapLevel;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.map.chunkdata.parts.SurfaceDataPart;

public class DataOverlay extends AbstractFullscreenOverlay {
    public DataOverlay(ScreenMapRenderer parent) {
        super(parent);
    }

    @Override
    public void renderOverlay(PoseStack stack, int mouseX, int mouseY) {
        int x = (int) (parent.topX + parent.xDiam * mouseX / parent.width);
        int z = (int) (parent.topZ + parent.zDiam * mouseY / parent.height);

        MapLevel level = MinimapApi.getInstance().getMapLevel(minecraft.level);
        ChunkData chunk = level.getChunk(ChunkLocation.locationForChunkPos(level, x >> 4, z >> 4));
        int y = 0;
        String biome = "unknown";
        String block = "unknown";
        byte light = 0;
        if (chunk != null) {
            SurfaceDataPart surface = chunk.getData(SurfaceDataPart.class);
            if (surface != null) {
                y = surface.heightmap[SurfaceDataPart.blockPosToIndex(x, z)];
                block = chunk.getResourceLocation(surface.blockid[SurfaceDataPart.blockPosToIndex(x, z)]).toString();
                biome = chunk.getResourceLocation(surface.biomeid[SurfaceDataPart.blockPosToIndex(x, z)]).toString();
                light = surface.blocklight[SurfaceDataPart.blockPosToIndex(x, z)];
            }
        }
        minecraft.font.draw(stack, String.format("%d, %d, %d  %s/%s %d", x, y, z, biome, block, light), 50, parent.height - 10, 0xFFFFFF);
    }

}
