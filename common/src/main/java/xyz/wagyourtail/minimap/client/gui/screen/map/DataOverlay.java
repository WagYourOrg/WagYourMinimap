package xyz.wagyourtail.minimap.client.gui.screen.map;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.chunkdata.parts.LightDataPart;
import xyz.wagyourtail.minimap.chunkdata.parts.SurfaceDataPart;
import xyz.wagyourtail.minimap.map.MapServer;

@SettingsContainer("gui.wagyourminimap.settings.overlay.data")
public class DataOverlay extends AbstractFullscreenOverlay {
    public DataOverlay(ScreenMapRenderer parent) {
        super(parent);
    }

    @Override
    public void renderOverlay(GuiGraphics stack, int mouseX, int mouseY) {
        int x = (int) (parent.topX + parent.xDiam * mouseX / parent.width);
        int z = (int) (parent.topZ + parent.zDiam * mouseY / parent.height);

        assert minecraft.level != null;
        MapServer.MapLevel level = MinimapApi.getInstance().getMapServer().getLevelFor(minecraft.level);
        ChunkData chunk = ChunkLocation.locationForChunkPos(level, x >> 4, z >> 4).get();
        int y = 0;
        String biome = "unknown";
        String block = "unknown";
        byte light = 0;
        if (chunk != null) {
            SurfaceDataPart surface = chunk.getData(SurfaceDataPart.class).orElse(null);
            if (surface != null) {
                y = surface.heightmap[SurfaceDataPart.blockPosToIndex(x, z)];
                block = BuiltInRegistries.BLOCK.getKey(chunk.getBlockState(surface.blockid[SurfaceDataPart.blockPosToIndex(
                    x,
                    z
                )]).getBlock()).toString();
                biome = chunk.getBiome(surface.biomeid[SurfaceDataPart.blockPosToIndex(x, z)]).toString();
            }
            LightDataPart lightData = chunk.getData(LightDataPart.class).orElse(null);
            if (lightData != null) {
                light = lightData.blocklight[SurfaceDataPart.blockPosToIndex(x, z)];
            }
        }
        stack.drawString(minecraft.font, level.parent().server_slug, 50, 4, 0xFFFFFF);
        stack.drawString(
                minecraft.font,
            String.format("%d, %d, %d  %s/%s %d", x, y, z, biome, block, light),
            50,
            parent.height - 10,
            0xFFFFFF
        );
    }

}
