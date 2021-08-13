package xyz.wagyourtail.oldminimap.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import xyz.wagyourtail.LazyResolver;
import xyz.wagyourtail.oldminimap.client.WagYourMinimapClient;
import xyz.wagyourtail.oldminimap.client.gui.image.VanillaMapImageStrategy;
import xyz.wagyourtail.oldminimap.scanner.ChunkData;
import xyz.wagyourtail.oldminimap.scanner.MapLevel;
import xyz.wagyourtail.oldminimap.scanner.MapRegion;

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

        float posX = parent.config.snapSide.right ? w - minimapSize - 5 : parent.config.snapSide.center ? w / 2f - minimapSize / 2f : 5;
        float posZ = parent.config.snapSide.bottom ? h - minimapSize - client.font.lineHeight - 10 : 5;
        //pull back map to 0, 0
        matrixStack.translate(posX, posZ, 0);
        Vec3 player_pos = player.getPosition(tickDelta);
        float player_rot = player.getYRot();
        renderer.renderMinimap(matrixStack, player_pos, minimapSize, player_pos, player_rot);
        matrixStack.popPose();

        matrixStack.pushPose();
        renderPlayerPosUnderMap(matrixStack, player_pos, w, h, minimapSize);
        //TODO: make toggle
        renderDebugInfo(matrixStack, player_pos, w, h, minimapSize);
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
            ChunkData chunk = cdata.resolveAsync(0);
            if (chunk != null) {
                String[] debugInfo = {
                    chunk.resources.get(chunk.blockid[ChunkData.blockPosToIndex(new BlockPos(player))]).toString(), // block
                    chunk.resources.get(chunk.biomeid[ChunkData.blockPosToIndex(new BlockPos(player))]).toString(), // biome
                    String.format("%08x", VanillaMapImageStrategy.getBlockColor(chunk.resources.get(chunk.blockid[ChunkData.blockPosToIndex(new BlockPos(player))]))), // block-color
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
