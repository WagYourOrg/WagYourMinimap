package xyz.wagyourtail.minimap.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import xyz.wagyourtail.minimap.client.WagYourMinimapClient;
import xyz.wagyourtail.minimap.data.updater.ChunkLoadStrategy;
import xyz.wagyourtail.minimap.server.WagYourMinimapServer;

public class WagYourMinimapFabric implements ClientModInitializer, DedicatedServerModInitializer {
    @Override
    public void onInitializeClient() {
        WagYourMinimapClient.init();
        ClientChunkEvents.CHUNK_LOAD.register((level, chunk) -> ChunkLoadStrategy.LOAD.invoker().onLoadChunk(chunk, level));
    }

    @Override
    public void onInitializeServer() {
        WagYourMinimapServer.init();
    }

}
