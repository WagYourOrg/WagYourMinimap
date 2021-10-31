package xyz.wagyourtail.minimap.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.KeyMapping;
import xyz.wagyourtail.minimap.WagYourMinimap;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.client.gui.InGameWaypointRenderer;
import xyz.wagyourtail.minimap.map.MapServer;
import xyz.wagyourtail.minimap.map.chunkdata.cache.ZipCacher;
import xyz.wagyourtail.minimap.map.chunkdata.updater.BlockUpdateStrategy;
import xyz.wagyourtail.minimap.map.chunkdata.updater.ChunkLoadStrategy;

import java.lang.reflect.InvocationTargetException;

public class WagYourMinimapClient extends WagYourMinimap {
    private static final KeyMapping key_openmap = new KeyMapping("key.wagyourminimap.openmap", InputConstants.KEY_M, "WagYourMinimap");

    public static void init() {
        KeyMappingRegistry.register(key_openmap);
        //client api getInstance first so we establish the instance as a ClientApi.
        MinimapClientApi.getInstance();

        try {
            MinimapApi.getInstance().addCacher(ZipCacher.class);
            MinimapApi.getInstance().registerChunkUpdateStrategy(ChunkLoadStrategy.class);
            MinimapApi.getInstance().registerChunkUpdateStrategy(BlockUpdateStrategy.class);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        ClientGuiEvent.RENDER_HUD.register(MinimapClientApi.getInstance().inGameHud::render);

        ClientTickEvent.CLIENT_POST.register((mc) -> {
            if (key_openmap.consumeClick()) {
                mc.setScreen(MinimapClientApi.getInstance().screen);
            }
        });
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register((player) -> {
            LOGGER.info("exiting {}", MinimapClientApi.getInstance().getMapServer());
            MinimapClientApi.getInstance().getMapServer().close();
            int i = 0;
            int j;
            while ((j = MinimapApi.getInstance().getSaving()) > 0) {
                if (i != j) LOGGER.info("Minimap Saving Chunks, (Remaining: {})", j);
                i = j;
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                Thread.yield();
            }
        });
        InGameWaypointRenderer.RENDER_LAST.register(MinimapClientApi.getInstance().waypointRenderer::onRender);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            MinimapApi.getInstance().close();
            try {
                MapServer.waitForSaveQueue();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));
    }

}
