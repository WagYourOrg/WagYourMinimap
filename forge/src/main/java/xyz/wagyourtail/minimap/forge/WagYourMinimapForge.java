package xyz.wagyourtail.minimap.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmlclient.ConfigGuiHandler;
import xyz.wagyourtail.minimap.WagYourMinimap;
import xyz.wagyourtail.minimap.client.WagYourMinimapClient;

@Mod(WagYourMinimap.MOD_ID)
public class WagYourMinimapForge {
    public WagYourMinimapForge() {
        EventBuses.registerModEventBus(WagYourMinimap.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientInit);
    }

    public void onClientInit(FMLClientSetupEvent setup) {
        WagYourMinimapClient.init();
        ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, () -> new ConfigGuiHandler.ConfigGuiFactory((mc, parent) -> ((WagYourMinimapClient) WagYourMinimap.INSTANCE).config.getConfigScreen(parent)));
    }
}
