package xyz.wagyourtail.minimap.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import xyz.wagyourtail.minimap.WagYourMinimap;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.client.gui.renderer.SquareMapNoRotRenderer;
import xyz.wagyourtail.minimap.data.updater.BlockUpdateStrategy;
import xyz.wagyourtail.minimap.data.updater.ChunkLoadStrategy;

import java.lang.reflect.InvocationTargetException;

public class WagYourMinimapClient extends WagYourMinimap {
    private static final KeyMapping key_openmap = new KeyMapping("key.wagyourminimap.openmap", InputConstants.KEY_M, "WagYourMinimap");

    protected static final Minecraft mc = Minecraft.getInstance();

    public static void init() {
        MinimapClientApi.getInstance();
        try {
            MinimapClientApi.inGameHud.setRenderer(SquareMapNoRotRenderer.class);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        KeyMappingRegistry.register(key_openmap);

        ClientGuiEvent.RENDER_HUD.register(MinimapClientApi.inGameHud::render);
        ClientTickEvent.CLIENT_POST.register((mc) -> {
            if (key_openmap.consumeClick()) {
                mc.setScreen(MinimapClientApi.getInstance().getConfig().getConfigScreen(null));
            }
        });
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register((player) -> {
            MinimapClientApi.getInstance().setCurrentLevel(null);
        });

        MinimapClientApi.getInstance().registerChunkUpdateStrategy(ChunkLoadStrategy.class);
        MinimapClientApi.getInstance().registerChunkUpdateStrategy(BlockUpdateStrategy.class);
    }
}
