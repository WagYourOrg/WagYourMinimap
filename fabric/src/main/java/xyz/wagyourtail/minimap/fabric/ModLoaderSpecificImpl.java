package xyz.wagyourtail.minimap.fabric;

import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import xyz.wagyourtail.minimap.ModLoaderSpecific;

public class ModLoaderSpecificImpl implements ModLoaderSpecific {

    public void checkEnableStencil() {
        ((IRenderTarget) Minecraft.getInstance().getMainRenderTarget()).enableStencil();
    }


    public void clientCommandContextLog(SharedSuggestionProvider p, Component s) {
        ((FabricClientCommandSource) p).sendFeedback(s);
    }
}
