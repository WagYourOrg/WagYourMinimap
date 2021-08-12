package xyz.wagyourtail.minimap.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import xyz.wagyourtail.LazyResolver;
import xyz.wagyourtail.minimap.WagYourMinimap;
import xyz.wagyourtail.minimap.client.WagYourMinimapClient;
import xyz.wagyourtail.minimap.client.WagYourMinimapClientConfig;
import xyz.wagyourtail.minimap.client.gui.renderer.VanillaMapRenderStrategy;
import xyz.wagyourtail.minimap.scanner.ChunkData;
import xyz.wagyourtail.minimap.scanner.MapLevel;
import xyz.wagyourtail.minimap.scanner.MapRegion;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class InGameHud extends AbstractMapGui {

    public InGameHud(WagYourMinimapClient parent) {
        super(parent);
    }

    @Override
    public void render(@NotNull PoseStack matrixStack, float tickDelta) {
        if (client.options.renderDebug) return;
        matrixStack.pushPose();
        int w = client.getWindow().getGuiScaledWidth();
        int h = client.getWindow().getGuiScaledHeight();

        float minimapSize = Math.min(w, h) * parent.config.mapScreenPercent;

        LocalPlayer player = client.player;
        assert player != null;
        //TODO add circular mode toggle
        renderSquareMinimap(matrixStack, player.getPosition(tickDelta), w, h, minimapSize);
        renderPlayerPosUnderMap(matrixStack, player.getPosition(tickDelta), w, h, minimapSize);
        //TODO: make toggle
        renderDebugInfo(matrixStack, player.getPosition(tickDelta), w, h, minimapSize);
        matrixStack.popPose();
    }

    private static final ResourceLocation player_icon_tex = new ResourceLocation(WagYourMinimap.MOD_ID, "textures/player_arrow.png");

    public void renderSquareMinimap(PoseStack matrixStack, @NotNull Vec3 player, int w, int h, float minimapSize) {
        matrixStack.pushPose();
        //because they're doubles we need to do this...
        float blockX = (float) (player.x % 16);
        float blockZ = (float) (player.z % 16);
        if (blockX < 0) blockX += 16;
        if (blockZ < 0) blockZ += 16;

        float posX = parent.config.snapSide.right ? w - minimapSize - 5 : parent.config.snapSide.center ? w / 2f - minimapSize / 2f : 5;
        float posZ = parent.config.snapSide.bottom ? h - minimapSize - client.font.lineHeight - 10 : 5;
        //pull back map to 0, 0
        matrixStack.translate(posX, posZ, 0);

        int chunkRadius = ((WagYourMinimapClientConfig)WagYourMinimap.INSTANCE.config).minimapChunkRadius;
        int chunkDiam = chunkRadius * 2 - 1;
        float chunkScale = minimapSize / ((float) chunkDiam - 1);

        int topChunkX = (((int) Math.floor(player.x)) >> 4) - chunkRadius;
        int topChunkZ = (((int) Math.floor(player.z)) >> 4) - chunkRadius;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        
        int i = 0;
        int j = 0;
        float partialX = blockX / 16f * chunkScale;
        float partialZ = blockZ / 16f * chunkScale;

        drawPartialChunk(matrixStack, getChunk(topChunkX + i, topChunkZ + j), chunkScale * i, chunkScale * j, chunkScale, blockX, blockZ, 16, 16);
        for (++j; j < chunkDiam - 1; ++j) {
            drawPartialChunk(matrixStack, getChunk(topChunkX + i, topChunkZ + j), chunkScale * i, chunkScale * j - partialZ, chunkScale, blockX, 0, 16, 16);
        }
        drawPartialChunk(matrixStack, getChunk(topChunkX + i, topChunkZ + j), chunkScale * i, chunkScale * j - partialZ, chunkScale, blockX, 0, 16, blockZ);
        for (++i; i < chunkDiam - 1; ++i) {
            j = 0;
            drawPartialChunk(matrixStack, getChunk(topChunkX + i, topChunkZ + j), chunkScale * i - partialX, chunkScale * j, chunkScale, 0, blockZ, 16, 16);
            for (++j; j < chunkDiam - 1; ++j) {
                drawChunk(matrixStack, getChunk(topChunkX + i, topChunkZ + j), chunkScale * i - partialX, chunkScale * j - partialZ, chunkScale);
            }
            drawPartialChunk(matrixStack, getChunk(topChunkX + i, topChunkZ + j), chunkScale * i - partialX, chunkScale * j - partialZ, chunkScale, 0, 0, 16, blockZ);
        }
        j = 0;
        drawPartialChunk(matrixStack, getChunk(topChunkX + i, topChunkZ + j), chunkScale * i - partialX, chunkScale * j, chunkScale, 0, blockZ, blockX, 16);
        for (++j; j < chunkDiam - 1; ++j) {
            drawPartialChunk(matrixStack, getChunk(topChunkX + i, topChunkZ + j), chunkScale * i - partialX, chunkScale * j - partialZ, chunkScale, 0, 0, blockX, 16);
        }
        drawPartialChunk(matrixStack, getChunk(topChunkX + i, topChunkZ + j), chunkScale * i - partialX, chunkScale * j - partialZ, chunkScale, 0, 0, blockX, blockZ);

        matrixStack.popPose();
    }

    public void renderPlayerPosUnderMap(PoseStack matrixStack, @NotNull Vec3 player, int w, int h, float minimapSize) {
        String pos = String.format("%.2f %.2f %.2f", player.x, player.y, player.z);
        int width = client.font.width(pos);
        float xPos = parent.config.snapSide.right ? w - width - 5 : parent.config.snapSide.center ? w / 2f - width / 2f : 5;
        float yPos = parent.config.snapSide.bottom ? h - client.font.lineHeight - 5 : minimapSize + 10;
        client.font.draw(matrixStack, pos, xPos, yPos, 0xFFFFFF);
    }

    public void renderDebugInfo(PoseStack matrixStack, @NotNull Vec3 player, int w, int h, float minimapSize) {
        int chunkX = ((int) player.x) >> 4;
        int chunkZ = ((int) player.z) >> 4;

        try {
            MapRegion region = parent.currentLevel.getRegion(new MapLevel.Pos(chunkX >> 5, chunkZ >> 5));
            LazyResolver<ChunkData> cdata = region.data[MapRegion.chunkPosToIndex(chunkX, chunkZ)];
            if (cdata == null) return;
            ChunkData chunk = cdata.resolveAsync(1);
            if (chunk != null) {
                String[] debugInfo = {
                    chunk.resources.get(chunk.blockid[ChunkData.blockPosToIndex(new BlockPos(player))]).toString(), // block
                    chunk.resources.get(chunk.biomeid[ChunkData.blockPosToIndex(new BlockPos(player))]).toString(), // biome
                    String.format("%08x", VanillaMapRenderStrategy.getBlockColor(chunk.resources.get(chunk.blockid[ChunkData.blockPosToIndex(new BlockPos(player))]))), // block-color
                };
                for (int i = 0; i < debugInfo.length; ++i) {
                    int width = client.font.width(debugInfo[i]);
                    float xPos = parent.config.snapSide.right ? w - width - 5 : parent.config.snapSide.center ? w / 2f - width / 2f : 5;
                    float yPos = parent.config.snapSide.bottom ? h - minimapSize - (client.font.lineHeight * (i + 1)) - 10 : minimapSize + 10 + client.font.lineHeight * (i + 1);

                    client.font.draw(matrixStack, debugInfo[i], xPos, yPos, 0xFFFFFF);
                }
            }
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    public enum SnapSide {
        TOP_LEFT(false, false, false), TOP_CENTER(false, true, false), TOP_RIGHT(true, false, false),
        BOTTOM_LEFT(false, false, true), BOTTOM_RIGHT(true, false, true);

        public final boolean right, center, bottom;

        SnapSide(boolean right, boolean center, boolean bottom) {
            this.right = right;
            this.center = center;
            this.bottom = bottom;
        }
    }
}
