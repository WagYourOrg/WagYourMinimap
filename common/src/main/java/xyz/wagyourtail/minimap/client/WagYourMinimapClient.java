package xyz.wagyourtail.minimap.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.event.events.common.ChunkEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;
import xyz.wagyourtail.minimap.WagYourMinimap;
import xyz.wagyourtail.minimap.client.hud.InGameHud;
import xyz.wagyourtail.minimap.scanner.MapLevel;

import java.util.concurrent.ExecutionException;

public class WagYourMinimapClient extends WagYourMinimap<WagYourMinimapClientConfig> {
    private static final KeyMapping key_openmap = new KeyMapping("key.wagyourminimap.openmap", InputConstants.KEY_M, "WagYourMinimap");

    protected static final Minecraft mc = Minecraft.getInstance();

    public static void init() {
        INSTANCE = new WagYourMinimapClient();
    }

    public final InGameHud inGameHud;

    public WagYourMinimapClient() {
        super(WagYourMinimapClientConfig.class);
        inGameHud = new InGameHud(this);
        KeyMappingRegistry.register(key_openmap);

        ClientGuiEvent.RENDER_HUD.register(inGameHud::render);
        ClientTickEvent.CLIENT_POST.register((mc) -> {
            if (key_openmap.consumeClick()) {
                mc.setScreen(this.config.getConfigScreen(null));
            }
        });

        ChunkEvent.LOAD_DATA.register((ChunkAccess chunk, @Nullable ServerLevel level, CompoundTag nbt) -> {
            assert mc.level != null;
            String server_slug = getServerName();
            String level_slug = getLevelName(level);
            if (currentLevel == null ||
                !currentLevel.server_slug.equals(server_slug) ||
                !currentLevel.level_slug.equals(level_slug)) {
                currentLevel = new MapLevel(server_slug, level_slug);
            }
            try {
                currentLevel.onServerChunk(chunk);
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });


    }

    public String getServerName() {
        IntegratedServer server = mc.getSingleplayerServer();
        if (server != null) {
            return "LOCAL_" + server.getWorldPath(LevelResource.ROOT).getFileName();
        }
        ServerData multiplayerServer = mc.getCurrentServer();
        if (multiplayerServer != null) {
            if (mc.isConnectedToRealms()) {
                return "REALM_" + multiplayerServer.name;
            }
            if (multiplayerServer.isLan()) {
                return "LAN_" + multiplayerServer.name;
            }
            return multiplayerServer.ip.replace(":25565", "").replace(":", "_");
        }
        return "UNKNOWN_SERVER_NAME";
    }

    public String getLevelName(ServerLevel level) {
        assert mc.level != null;
        if (level != null) {
            return level.dimension().location().toString().replace(":", "_");
        }
        return mc.level.dimension().location().toString().replace(":", "_");
    }
}
