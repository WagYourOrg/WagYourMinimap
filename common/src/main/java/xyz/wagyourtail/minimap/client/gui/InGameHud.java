package xyz.wagyourtail.minimap.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import xyz.wagyourtail.ResolveQueue;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.client.gui.image.VanillaMapImageStrategy;
import xyz.wagyourtail.minimap.map.MapLevel;

public class InGameHud extends AbstractMapGui {

    @Override
    public void render(@NotNull PoseStack matrixStack, float tickDelta) {
        if (client.options.renderDebug) return;
        matrixStack.pushPose();
        int w = client.getWindow().getGuiScaledWidth();
        int h = client.getWindow().getGuiScaledHeight();

        float minimapSize = Math.min(w, h) * MinimapClientApi.getInstance().getConfig().mapScreenPercent;

        LocalPlayer player = client.player;
        assert player != null;

        boolean bottom = MinimapClientApi.getInstance().getConfig().snapSide.bottom;

        float posX = MinimapClientApi.getInstance().getConfig().snapSide.right ? w - minimapSize - 5 : MinimapClientApi.getInstance().getConfig().snapSide.center ? w / 2f - minimapSize / 2f : 5;
        float posZ = bottom ? h - minimapSize - client.font.lineHeight - 10 : 5;
        Vec3 player_pos = player.getPosition(tickDelta);
        float player_rot = player.getYRot();

        //pull back map to 0, 0
        matrixStack.translate(posX, posZ, 0);
        renderer.renderMinimap(matrixStack, player_pos, minimapSize, player_pos, player_rot);
        matrixStack.popPose();

        //pull back text pos to 0, 0
        matrixStack.pushPose();
        matrixStack.translate(posX, posZ, 0);
        if (!bottom) {
            matrixStack.translate(0, minimapSize + 5, 0);
        }
        renderer.renderText(matrixStack, minimapSize, bottom, new TextComponent(String.format("%.2f %.2f %.2f", player_pos.x, player_pos.y, player_pos.z)));
        matrixStack.popPose();

//        matrixStack.pushPose();
//
//        renderPlayerPosUnderMap(matrixStack, player_pos, w, h, minimapSize);
//        //TODO: make toggle
//        renderDebugInfo(matrixStack, player_pos, w, h, minimapSize);
//        matrixStack.popPose();
    }

    public void renderPlayerPosUnderMap(PoseStack matrixStack, @NotNull Vec3 player, int w, int h, float minimapSize) {
        String pos = String.format("%.2f %.2f %.2f", player.x, player.y, player.z);
        int width = client.font.width(pos);
        float xPos = MinimapClientApi.getInstance().getConfig().snapSide.right ? w - width - 5 : MinimapClientApi.getInstance().getConfig().snapSide.center ? w / 2f - width / 2f : 5;
        float yPos = MinimapClientApi.getInstance().getConfig().snapSide.bottom ? h - client.font.lineHeight - 5 : minimapSize + 10;
        client.font.draw(matrixStack, pos, xPos, yPos, 0xFFFFFF);
    }

    public void renderDebugInfo(PoseStack matrixStack, @NotNull Vec3 player, int w, int h, float minimapSize) {
        int chunkX = ((int) player.x) >> 4;
        int chunkZ = ((int) player.z) >> 4;

        MapLevel level = MinimapClientApi.getInstance().getMapLevel(client.level);
        if (level == null) return;
        ResolveQueue<ChunkData> cdata = level.getChunk(ChunkLocation.locationForChunkPos(level, chunkX, chunkZ));
        if (cdata == null) return;
        ChunkData chunk = cdata.getNow();
        if (chunk != null) {
            String[] debugInfo = {
                chunk.getResourceLocation(chunk.blockid[ChunkData.blockPosToIndex(new BlockPos(player))]).toString(), // block
                chunk.getResourceLocation(chunk.biomeid[ChunkData.blockPosToIndex(new BlockPos(player))]).toString(), // biome
                String.format("%08x", VanillaMapImageStrategy.getBlockColor(chunk.getResourceLocation(chunk.blockid[ChunkData.blockPosToIndex(new BlockPos(player))]))), // block-color
            };
            for (int i = 0; i < debugInfo.length; ++i) {
                int width = client.font.width(debugInfo[i]);
                float xPos = MinimapClientApi.getInstance().getConfig().snapSide.right ? w - width - 5 : MinimapClientApi.getInstance().getConfig().snapSide.center ? w / 2f - width / 2f : 5;
                float yPos = MinimapClientApi.getInstance().getConfig().snapSide.bottom ? h - minimapSize - (client.font.lineHeight * (i + 1)) - 10 : minimapSize + 10 + client.font.lineHeight * (i + 1);

                client.font.draw(matrixStack, debugInfo[i], xPos, yPos, 0xFFFFFF);
            }
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
