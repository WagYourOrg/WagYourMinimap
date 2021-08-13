package xyz.wagyourtail.minimap.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import xyz.wagyourtail.oldminimap.client.WagYourMinimapClient;
import xyz.wagyourtail.oldminimap.server.WagYourMinimapServer;

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
