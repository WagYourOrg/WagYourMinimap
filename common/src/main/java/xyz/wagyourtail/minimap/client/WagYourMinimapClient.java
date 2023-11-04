package xyz.wagyourtail.minimap.client;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import xyz.wagyourtail.minimap.WagYourMinimap;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.api.client.config.MinimapClientConfig;
import xyz.wagyourtail.minimap.chunkdata.cache.InMemoryCacher;
import xyz.wagyourtail.minimap.chunkdata.cache.ZipCacher;
import xyz.wagyourtail.minimap.chunkdata.updater.LightDataUpdater;
import xyz.wagyourtail.minimap.chunkdata.updater.SurfaceDataUpdater;
import xyz.wagyourtail.minimap.chunkdata.updater.UndergroundDataUpdater;
import xyz.wagyourtail.minimap.client.gui.hud.InGameHud;
import xyz.wagyourtail.minimap.client.gui.screen.WaypointEditScreen;
import xyz.wagyourtail.minimap.client.world.InGameWaypointRenderer;
import xyz.wagyourtail.minimap.map.MapServer;
import xyz.wagyourtail.minimap.waypoint.Waypoint;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WagYourMinimapClient extends WagYourMinimap {
    public static final Event<ClientCommandRegistrationEvent> CLIENT_COMMAND_REGISTRATION_EVENT = EventFactory.createLoop();

    private static final KeyMapping key_openmap = new KeyMapping(
        "key.wagyourminimap.openmap",
        InputConstants.KEY_M,
        "WagYourMinimap"
    );
    private static final KeyMapping key_fullscreen_minimap = new KeyMapping(
        "key.wagyourminimap.fullscreen_minimap",
        InputConstants.KEY_N,
        "WagYourMinimap"
    );
    private static final KeyMapping key_new_waypoint = new KeyMapping(
        "key.wagyourminimap.new_waypoint",
        InputConstants.KEY_B,
        "WagYourMinimap"
    );
    private static final KeyMapping key_zoom_in = new KeyMapping(
        "key.wagyourminimap.zoom_in",
        InputConstants.KEY_EQUALS,
        "WagYourMinimap"
    );
    private static final KeyMapping key_zoom_out = new KeyMapping(
        "key.wagyourminimap.zoom_out",
        InputConstants.KEY_MINUS,
        "WagYourMinimap"
    );
    private static final KeyMapping key_hide_minimap = new KeyMapping(
        "key.wagyourminimap.hide_minimap",
        InputConstants.UNKNOWN.getValue(),
        "WagYourMinimap"
    );
    protected static final Minecraft minecraft = Minecraft.getInstance();

    public static void init() {
        KeyMappingRegistry.register(key_openmap);
        KeyMappingRegistry.register(key_fullscreen_minimap);
        KeyMappingRegistry.register(key_new_waypoint);
        KeyMappingRegistry.register(key_zoom_in);
        KeyMappingRegistry.register(key_zoom_out);
        KeyMappingRegistry.register(key_hide_minimap);

        //client api getInstance first so we establish the instance as a ClientApi.
        MinimapClientApi.getInstance();

        MinimapApi.getInstance().cacheManager.addCacherAfter(new ZipCacher(), null);
        MinimapApi.getInstance().cacheManager.addCacherBefore(new InMemoryCacher(), ZipCacher.class);
        MinimapApi.getInstance().registerChunkUpdateStrategy(SurfaceDataUpdater.class);
        MinimapApi.getInstance().registerChunkUpdateStrategy(UndergroundDataUpdater.class);
        MinimapApi.getInstance().registerChunkUpdateStrategy(LightDataUpdater.class);




        ClientGuiEvent.RENDER_HUD.register((matrix, delta) -> {
            try {
                if (MinimapApi.getInstance().getConfig().get(MinimapClientConfig.class).showMinimap) {
                    InGameHud.render(matrix, delta);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });

        ClientTickEvent.CLIENT_POST.register((mc) -> {
            if (key_openmap.consumeClick()) {
                mc.setScreen(MinimapClientApi.getInstance().screen);
            }
            if (key_fullscreen_minimap.isDown()) {
                InGameHud.getRenderer().fullscreen_toggle = true;
            } else {
                InGameHud.getRenderer().fullscreen_toggle = false;
            }
            if (key_new_waypoint.consumeClick()) {
                mc.setScreen(WaypointEditScreen.createNewFromPos(null, BlockPos.containing(mc.cameraEntity.getEyePosition())));
            }
            if (key_zoom_in.consumeClick()) {
                int rad = MinimapApi.getInstance().getConfig().get(MinimapClientConfig.class).getChunkRadius();
                if (rad > MinimapClientConfig.MIN_CHUNK_RADIUS) {
                    --rad;
                }
                MinimapApi.getInstance().getConfig().get(MinimapClientConfig.class).setChunkRadius(rad);
            }
            if (key_zoom_out.consumeClick()) {
                int rad = MinimapApi.getInstance().getConfig().get(MinimapClientConfig.class).getChunkRadius();
                if (rad < MinimapClientConfig.MAX_CHUNK_RADIUS) {
                    ++rad;
                }
                MinimapApi.getInstance().getConfig().get(MinimapClientConfig.class).setChunkRadius(rad);
            }
            if (key_hide_minimap.consumeClick()) {
                MinimapApi.getInstance()
                    .getConfig()
                    .get(MinimapClientConfig.class).showMinimap = !MinimapApi.getInstance().getConfig().get(
                    MinimapClientConfig.class).showMinimap;
                MinimapApi.getInstance().getConfig().saveConfig();
            }
        });
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register((player) -> {
            LOGGER.info("exiting {}", MinimapClientApi.getInstance().getMapServer());
            int i = 0;
            int j;
            while ((j = MinimapApi.getInstance().getSaving()) > 0) {
                if (i != j) {
                    LOGGER.info("Minimap Saving Chunks, (Remaining: {})", j);
                }
                i = j;
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //                Thread.yield();
            }
        });
        InGameWaypointRenderer.RENDER_LAST.register((stack, camera) -> {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            try {
                if (MinimapApi.getInstance().getConfig().get(MinimapClientConfig.class).showWaypoints) {
                    InGameWaypointRenderer.onRender(constructGuiGraphics(minecraft, stack), camera);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            MinimapApi.getInstance().close();
            try {
                MapServer.waitForSaveQueue();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));
        ClientPlayerEvent.CLIENT_PLAYER_RESPAWN.register((oldP, newP) -> {
            try {
                if (oldP != null && oldP.getHealth() <= 0) {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yy/MM/dd HH:mm:ss");
                    LocalDateTime now = LocalDateTime.now();
                    String[] dims = new String[] {
                        MinimapApi.getInstance()
                            .getMapServer()
                            .getLevelFor(oldP.level()).level_slug()
                    };
                    MinimapApi.getInstance().getMapServer().waypoints.forceAddWaypoint(
                        new Waypoint(
                            oldP.level().dimensionType().coordinateScale(),
                            oldP.getBlockX(),
                            oldP.getBlockY(),
                            oldP.getBlockZ(),
                            (byte) 0xFF,
                            (byte) 0xFF,
                            (byte) 0xFF,
                            "Death @ " + dtf.format(now),
                            new String[] {"deaths"},
                            dims,
                            new JsonObject(),
                            "skull",
                            true,
                            false
                        )
                    );
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
        CLIENT_COMMAND_REGISTRATION_EVENT.register(d -> {
            try {
                d.register((LiteralArgumentBuilder) MinimapClientApi.getInstance().getConfig().createSettingsCommand().getCommandTree("minimap"));
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public interface ClientCommandRegistrationEvent {
        void register(CommandDispatcher<? extends SharedSuggestionProvider> dispatcher);
    }
}
