package xyz.wagyourtail.minimap.api.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.storage.LevelResource;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.api.client.config.CurrentServerConfig;
import xyz.wagyourtail.minimap.api.client.config.MinimapClientConfig;
import xyz.wagyourtail.minimap.client.gui.screen.MapScreen;

public class MinimapClientApi extends MinimapApi {
    protected final Minecraft mc = Minecraft.getInstance();
    public final MapScreen screen = new MapScreen();

    protected MinimapClientApi() {
        super();
        config.registerConfig("client", MinimapClientConfig.class);
        config.registerConfig("perServer", CurrentServerConfig.class);
    }

    public static MinimapClientApi getInstance() {
        if (INSTANCE == null) {
            new MinimapClientApi();
        }
        return (MinimapClientApi) INSTANCE;
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
        LOGGER.warn("COULD NOT DETERMINE CURRENT SERVER NAME!");
        return "UNKNOWN_SERVER_NAME";
    }

    public void sendTp(int x, int y, int z) {
        assert mc.player != null;
        String command = MinimapApi.getInstance()
            .getConfig()
            .get(CurrentServerConfig.class)
            .getTpCommand()
            .replace("%player", mc.player.getGameProfile().getName())
            .replace("%x", Integer.toString(x))
            .replace("%y", Integer.toString(y))
            .replace("%z", Integer.toString(z));

        mc.player.chat(command);
    }

}
