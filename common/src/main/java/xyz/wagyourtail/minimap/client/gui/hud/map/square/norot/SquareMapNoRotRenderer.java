package xyz.wagyourtail.minimap.client.gui.hud.map.square.norot;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.api.client.config.MinimapClientConfig;
import xyz.wagyourtail.minimap.client.gui.hud.map.AbstractMapOverlayRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.map.AbstractMinimapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.map.square.SquareMapBorderOverlay;

public class SquareMapNoRotRenderer extends AbstractMinimapRenderer {

    public SquareMapNoRotRenderer() {
        overlays = new AbstractMapOverlayRenderer[] {new SquareMapBorderOverlay(this)};
    }

    @Override
    public void renderMinimap(PoseStack matrixStack, @NotNull Vec3 center, float maxLength, @NotNull Vec3 player_pos, float player_rot) {
        int chunkRadius = MinimapClientApi.getInstance().getConfig().get(MinimapClientConfig.class).chunkRadius;

        float blockX = (float) (center.x % 16);
        float blockZ = (float) (center.z % 16);
        if (blockX < 0) {
            blockX += 16;
        }
        if (blockZ < 0) {
            blockZ += 16;
        }

        int topChunkX = (((int) Math.floor(center.x)) >> 4) - chunkRadius + 1;
        int topChunkZ = (((int) Math.floor(center.z)) >> 4) - chunkRadius + 1;

        if (chunkRadius == 1) {
            topChunkX -= 1;
            topChunkZ -= 1;
        }

        int chunkDiam = chunkRadius * 2 - 1;
        float chunkScale = maxLength / ((float) chunkDiam - 1);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int i = 0;
        int j = 0;
        float partialX = blockX / 16f * chunkScale;
        float partialZ = blockZ / 16f * chunkScale;

        //DRAW CHUNKS
        drawPartialChunk(matrixStack,
            getChunk(topChunkX + i, topChunkZ + j),
            chunkScale * i,
            chunkScale * j,
            chunkScale,
            blockX,
            blockZ,
            16,
            16
        );
        for (++j; j < chunkDiam - 1; ++j) {
            drawPartialChunk(matrixStack,
                getChunk(topChunkX + i, topChunkZ + j),
                chunkScale * i,
                chunkScale * j - partialZ,
                chunkScale,
                blockX,
                0,
                16,
                16
            );
        }
        drawPartialChunk(matrixStack,
            getChunk(topChunkX + i, topChunkZ + j),
            chunkScale * i,
            chunkScale * j - partialZ,
            chunkScale,
            blockX,
            0,
            16,
            blockZ
        );
        for (++i; i < chunkDiam - 1; ++i) {
            j = 0;
            drawPartialChunk(matrixStack,
                getChunk(topChunkX + i, topChunkZ + j),
                chunkScale * i - partialX,
                chunkScale * j,
                chunkScale,
                0,
                blockZ,
                16,
                16
            );
            for (++j; j < chunkDiam - 1; ++j) {
                drawChunk(matrixStack,
                    getChunk(topChunkX + i, topChunkZ + j),
                    chunkScale * i - partialX,
                    chunkScale * j - partialZ,
                    chunkScale
                );
            }
            drawPartialChunk(matrixStack,
                getChunk(topChunkX + i, topChunkZ + j),
                chunkScale * i - partialX,
                chunkScale * j - partialZ,
                chunkScale,
                0,
                0,
                16,
                blockZ
            );
        }
        j = 0;
        drawPartialChunk(matrixStack,
            getChunk(topChunkX + i, topChunkZ + j),
            chunkScale * i - partialX,
            chunkScale * j,
            chunkScale,
            0,
            blockZ,
            blockX,
            16
        );
        for (++j; j < chunkDiam - 1; ++j) {
            drawPartialChunk(matrixStack,
                getChunk(topChunkX + i, topChunkZ + j),
                chunkScale * i - partialX,
                chunkScale * j - partialZ,
                chunkScale,
                0,
                0,
                blockX,
                16
            );
        }
        drawPartialChunk(matrixStack,
            getChunk(topChunkX + i, topChunkZ + j),
            chunkScale * i - partialX,
            chunkScale * j - partialZ,
            chunkScale,
            0,
            0,
            blockX,
            blockZ
        );
    }

    @Override
    public void renderText(PoseStack matrixStack, float maxLength, boolean bottom, Component... textLines) {
        float lineOffset = 0;
        for (Component textLine : textLines) {
            int len = minecraft.font.width(textLine);
            float scale = len / maxLength;
            if (scale > 1) {
                matrixStack.scale(1 / scale, 1 / scale, 0);
            }
            minecraft.font.drawShadow(matrixStack,
                textLine,
                len < maxLength ? (maxLength - len) / 2 : 0,
                lineOffset,
                0xFFFFFF
            );
            if (scale > 1) {
                matrixStack.scale(scale, scale, 0);
                lineOffset += scale * minecraft.font.lineHeight;
            } else {
                lineOffset += minecraft.font.lineHeight;
            }
        }
    }

}
