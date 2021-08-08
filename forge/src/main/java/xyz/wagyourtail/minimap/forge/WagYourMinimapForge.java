package xyz.wagyourtail.minimap.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmlclient.ConfigGuiHandler;
import xyz.wagyourtail.minimap.WagYourMinimap;
import xyz.wagyourtail.minimap.client.WagYourMinimapClient;
import xyz.wagyourtail.minimap.scanner.updater.BlockUpdateStrategy;

@Mod(WagYourMinimap.MOD_ID)
public class WagYourMinimapForge {
    public WagYourMinimapForge() {
        EventBuses.registerModEventBus(WagYourMinimap.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientInit);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void onClientInit(FMLClientSetupEvent setup) {
        WagYourMinimapClient.init();
        ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, () -> new ConfigGuiHandler.ConfigGuiFactory((mc, parent) -> ((WagYourMinimapClient) WagYourMinimap.INSTANCE).config.getConfigScreen(parent)));
    }

    @SubscribeEvent
    public void onBlockUpdate(BlockEvent block) {
        BlockUpdateStrategy.BLOCK_UPDATE_EVENT.invoker().onBlockUpdate(block.getPos(), (Level) block.getWorld());
    }
}
