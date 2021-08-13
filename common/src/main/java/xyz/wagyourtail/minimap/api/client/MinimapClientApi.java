package xyz.wagyourtail.minimap.api.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.storage.LevelResource;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.client.gui.InGameHud;
import xyz.wagyourtail.minimap.client.gui.image.AbstractImageStrategy;

public class MinimapClientApi extends MinimapApi {
    protected static final Minecraft mc = Minecraft.getInstance();
    public static final InGameHud inGameHud = new InGameHud();


    public static MinimapClientApi getInstance() {
        if (INSTANCE == null) INSTANCE = new MinimapClientApi();
        return (MinimapClientApi) INSTANCE;
    }

    @Override
    public synchronized WagYourMinimapClientConfig getConfig() {
        if (config == null) getConfig(WagYourMinimapClientConfig.class);
        return (WagYourMinimapClientConfig) config;
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

    public void invalidateAllImages() {
        for (AbstractImageStrategy renderLayer : inGameHud.renderer.getRenderLayers()) {
            renderLayer.invalidateAll();
        }
    }

    public void invalidateImages(AbstractImageStrategy.ChunkLocation location) {
        for (AbstractImageStrategy renderLayer : inGameHud.renderer.getRenderLayers()) {
            renderLayer.invalidateChunk(location);
        }
    }

    public String getLevelName() {
        return getLevelName(mc.level);
    }

}
