package xyz.wagyourtail.minimap.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import xyz.wagyourtail.minimap.WagYourMinimap;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.map.chunkdata.cache.ZipCacher;
import xyz.wagyourtail.minimap.map.chunkdata.updater.BlockUpdateStrategy;
import xyz.wagyourtail.minimap.map.chunkdata.updater.ChunkLoadStrategy;
import xyz.wagyourtail.minimap.client.gui.MapRendererBuilder;
import xyz.wagyourtail.minimap.client.gui.image.BlockLightImageStrategy;
import xyz.wagyourtail.minimap.client.gui.image.VanillaMapImageStrategy;
import xyz.wagyourtail.minimap.client.gui.renderer.SquareMapNoRotRenderer;
import xyz.wagyourtail.minimap.client.gui.renderer.overlay.SquareMapBorderOverlay;

import java.lang.reflect.InvocationTargetException;

public class WagYourMinimapClient extends WagYourMinimap {
    private static final KeyMapping key_openmap = new KeyMapping("key.wagyourminimap.openmap", InputConstants.KEY_M, "WagYourMinimap");

    public static void init() {
        KeyMappingRegistry.register(key_openmap);

        try {
            //client api getInstance first so we establish the instance as a ClientApi.
            MinimapClientApi.getInstance().inGameHud.setRenderer(MapRendererBuilder.createBuilder(SquareMapNoRotRenderer.class)
                .addRenderLayer(VanillaMapImageStrategy.class)
                .addRenderLayer(BlockLightImageStrategy.class)
                .addOverlay(SquareMapBorderOverlay.class)
                .build());
            MinimapApi.getInstance().addCacher(ZipCacher.class);
            MinimapApi.getInstance().registerChunkUpdateStrategy(ChunkLoadStrategy.class);
            MinimapApi.getInstance().registerChunkUpdateStrategy(BlockUpdateStrategy.class);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        ClientGuiEvent.RENDER_HUD.register(MinimapClientApi.getInstance().inGameHud::render);
        ClientTickEvent.CLIENT_POST.register((mc) -> {
            if (key_openmap.consumeClick()) {
                mc.setScreen(MinimapClientApi.getInstance().getConfig().getConfigScreen(null));
            }
        });
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register((player) -> {
            LOGGER.info("exiting {}", MinimapClientApi.getInstance().getMapServer());
            int i = 0;
            int j;
            while ((j = MinimapApi.getInstance().getSaving()) > 0) {
                if (i != j) LOGGER.info("Minimap Saving Chunks, (Remaining: {})", j);
                i = j;
                Thread.yield();
            }
        });
    }

}
