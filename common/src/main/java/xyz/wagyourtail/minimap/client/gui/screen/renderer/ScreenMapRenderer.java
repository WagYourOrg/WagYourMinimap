package xyz.wagyourtail.minimap.client.gui.screen.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;
import xyz.wagyourtail.minimap.client.gui.renderer.AbstractMapRenderer;
import xyz.wagyourtail.minimap.client.gui.screen.renderer.overlay.DataOverlay;
import xyz.wagyourtail.minimap.client.gui.screen.renderer.overlay.PlayerIconOverlay;
import xyz.wagyourtail.minimap.client.gui.screen.renderer.overlay.WaypointOverlay;

public class ScreenMapRenderer extends AbstractMapRenderer {

    public AbstractFullscreenOverlay[] overlays = new AbstractFullscreenOverlay[] {new PlayerIconOverlay(this), new WaypointOverlay(this), new DataOverlay(this)};

    public int blockRadius = 30 * 16;
    public int width, height, xDiam, zDiam;
    public float chunkWidth;
    public Vec3 center = Vec3.ZERO;
    public int chunkX, chunkZ, chunkXDiam, chunkZDiam;
    public float topX, topZ;
    public float blockX, blockZ, endBlockX, endBlockZ, offsetX, offsetZ;

    public void changeZoom(int radius) {
        this.blockRadius = Math.max(16, radius);
        computeDimensions(width, height);
        moveCenter(center);
    }

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
        moveCenter(center);
    }

    public void moveCenter(Vec3 center) {
        this.center = center;

        topX = (float) (center.x - xDiam / 2f);
        topZ = (float) (center.z - zDiam / 2f);

        blockX = topX % 16;
        blockZ = topZ % 16;
        if (blockX < 0) blockX += 16;
        if (blockZ < 0) blockZ += 16;

        endBlockX = (this.blockX + xDiam) % 16;
        endBlockZ = (this.blockZ + zDiam) % 16;

        chunkX = (int) (topX - blockX) >> 4;
        chunkZ = (int) (topZ - blockZ) >> 4;

        chunkXDiam = (int) Math.ceil(xDiam / 16f);
        chunkZDiam = (int) Math.ceil(zDiam / 16f);

        offsetX = this.blockX * chunkWidth / 16f;
        offsetZ = this.blockZ * chunkWidth / 16f;
    }


    public void renderMinimap(PoseStack matrixStack, int mouseX, int mouseY) {


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

        for (AbstractFullscreenOverlay overlay : overlays) {
            overlay.renderOverlay(matrixStack, mouseX, mouseY);
        }
    }


    public void setOverlays(AbstractFullscreenOverlay... overlays) {
        this.overlays = overlays;
    }
}
