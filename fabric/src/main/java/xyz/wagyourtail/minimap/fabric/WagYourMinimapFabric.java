package xyz.wagyourtail.minimap.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import xyz.wagyourtail.minimap.client.WagYourMinimapClient;
import xyz.wagyourtail.minimap.server.WagYourMinimapServer;

public class WagYourMinimapFabric implements ClientModInitializer, DedicatedServerModInitializer {
    @Override
    public void onInitializeClient() {
        WagYourMinimapClient.init();
    }

    @Override
    public void onInitializeServer() {
        WagYourMinimapServer.init();
    }

}
