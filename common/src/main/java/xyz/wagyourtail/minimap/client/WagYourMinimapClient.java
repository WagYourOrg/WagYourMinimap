package xyz.wagyourtail.minimap.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import xyz.wagyourtail.minimap.WagYourMinimap;
import xyz.wagyourtail.minimap.client.gui.InGameHud;

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
    }

    @Override
    public String getServerName() {
        IntegratedServer server = mc.getSingleplayerServer();
        if (server != null) {
            return "LOCAL_" + server.getWorldPath(LevelResource.ROOT).normalize().getFileName();
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

    @Override
    public String getLevelName(Level level) {
        if (level == null)
            return mc.level.dimension().location().toString().replace(":", "_");
        return level.dimension().location().toString().replace(":", "_");
    }

    @Override
    public Level resolveServerLevel(Level level) {
        if (level == null) return mc.level;
        return super.resolveServerLevel(level);
    }

}
