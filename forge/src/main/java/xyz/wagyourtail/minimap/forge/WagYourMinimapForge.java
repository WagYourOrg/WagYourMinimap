package xyz.wagyourtail.minimap.forge;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.brigadier.CommandDispatcher;
import dev.architectury.platform.forge.EventBuses;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import xyz.wagyourtail.minimap.WagYourMinimap;
import xyz.wagyourtail.minimap.chunkdata.updater.AbstractChunkDataUpdater;
import xyz.wagyourtail.minimap.client.WagYourMinimapClient;
import xyz.wagyourtail.minimap.client.gui.screen.SettingsScreen;
import xyz.wagyourtail.minimap.client.world.InGameWaypointRenderer;
import xyz.wagyourtail.minimap.server.WagYourMinimapServer;

@Mod(WagYourMinimap.MOD_ID)
public class WagYourMinimapForge {
    public WagYourMinimapForge() {
        EventBuses.registerModEventBus(WagYourMinimap.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientInit);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void onClientInit(FMLClientSetupEvent setup) {
        WagYourMinimapClient.init();
        ModLoadingContext.get().registerExtensionPoint(
            ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> new SettingsScreen(parent))
        );
    }

    public void onServerInit(FMLDedicatedServerSetupEvent setup) {
        WagYourMinimapServer.init();
    }

    @SubscribeEvent
    public void onBlockUpdate(BlockEvent block) {
        AbstractChunkDataUpdater.BLOCK_UPDATE.invoker().onBlockUpdate(block.getPos(), (Level) block.getLevel());
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load load) {
        AbstractChunkDataUpdater.CHUNK_LOAD.invoker().onLoadChunk(load.getChunk(), (Level) load.getLevel());
    }

    @SubscribeEvent
    public void onRenderLast(RenderLevelLastEvent renderEvent) {
//        if (renderEvent.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            InGameWaypointRenderer.RENDER_LAST.invoker().onRenderLast(
                renderEvent.getPoseStack(),
                Minecraft.getInstance().gameRenderer.getMainCamera()
            );
//        }
    }

    @SubscribeEvent
    public void onClientCommand(RegisterClientCommandsEvent clientCommandsEvent) {
        WagYourMinimapClient.CLIENT_COMMAND_REGISTRATION_EVENT.invoker().register(clientCommandsEvent.getDispatcher());
    }

}
