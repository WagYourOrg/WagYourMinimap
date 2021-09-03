package xyz.wagyourtail.minimap.client.gui.screen.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import xyz.wagyourtail.minimap.client.gui.renderer.AbstractMapRenderer;

public class ScreenMapRenderer extends AbstractMapRenderer {

    public int blockRadius = 30 * 16;
    public int width;
    public int height;
    private int xDiam, zDiam;
    public float chunkWidth;

    public void computeDimensions(int width, int height) {
        this.width = width;
        this.height = height;
        if (width < height) {
            xDiam = blockRadius;
            chunkWidth = width * 16 / (float) blockRadius;
            zDiam = height * xDiam / width;
        } else {
            zDiam = blockRadius;
            chunkWidth = height * 16 / (float) blockRadius;
            xDiam = width * zDiam / height;
        }
    }

    @Override
    public void renderMinimap(PoseStack matrixStack, @NotNull Vec3 center, float maxLength, @NotNull Vec3 player_pos, float player_rot) {
        int blockX = (int) (center.x % 16);
        int blockZ = (int) (center.z % 16);
        if (blockX < 0) blockX += 16;
        if (blockZ < 0) blockZ += 16;

        int endBlockX = (blockX + xDiam) % 16;
        int endBlockZ = (blockZ + zDiam) % 16;

        int topX = (int) center.x - xDiam / 2;
        int topZ = (int) center.z - zDiam / 2;

        int chunkX = topX >> 4;
        int chunkZ = topZ >> 4;

        int chunkXDiam = (int) Math.ceil(xDiam / 16f);
        int chunkZDiam = (int) Math.ceil(zDiam / 16f);

        float offsetX = blockX * chunkWidth / 16f;
        float offsetZ = blockZ * chunkWidth / 16f;

        drawPartialChunk(matrixStack, getChunk(chunkX, chunkZ), 0, 0, chunkWidth, blockX, blockZ, 16, 16);
        for (int j = 1; j < chunkZDiam; ++j) {
            drawPartialChunk(matrixStack, getChunk(chunkX, chunkZ + j), 0, j * chunkWidth - offsetZ, chunkWidth, blockX, 0, 16, 16);
        }
        drawPartialChunk(matrixStack, getChunk(chunkX, chunkZ + chunkZDiam), 0, chunkZDiam * chunkWidth - offsetZ, chunkWidth, blockX, 0, 16, endBlockZ);

        for (int i = 1; i < chunkXDiam; ++i) {
            drawPartialChunk(matrixStack, getChunk(chunkX + i, chunkZ), i * chunkWidth - offsetX, 0, chunkWidth, 0, blockZ, 16, 16);
            for (int j = 1; j < chunkZDiam; ++j) {
                drawChunk(matrixStack, getChunk(chunkX + i, chunkZ + j), i * chunkWidth - offsetX, j * chunkWidth - offsetZ, chunkWidth);
            }
            drawPartialChunk(matrixStack, getChunk(chunkX + i, chunkZ + chunkZDiam), i * chunkWidth - offsetX, chunkZDiam * chunkWidth - offsetZ, chunkWidth, 0, 0, 16, endBlockZ);
        }

        drawPartialChunk(matrixStack, getChunk(chunkX + chunkXDiam, chunkZ), chunkXDiam * chunkWidth - offsetX, 0, chunkWidth, 0, blockZ, endBlockX, 16);
        for (int j = 1; j < chunkZDiam; ++j) {
            drawPartialChunk(matrixStack, getChunk(chunkX + chunkXDiam, chunkZ + j), chunkXDiam * chunkWidth - offsetX, j * chunkWidth - offsetZ, chunkWidth, 0, 0, endBlockX, 16);
        }
        drawPartialChunk(matrixStack, getChunk(chunkX + chunkXDiam, chunkZ + chunkZDiam), chunkXDiam * chunkWidth - offsetX, chunkZDiam * chunkWidth - offsetZ, chunkWidth, 0, 0, endBlockX, endBlockZ);
    }

    @Override
    public void renderText(PoseStack matrixStack, float maxLength, boolean bottom, Component... textLines) {

    }

}
