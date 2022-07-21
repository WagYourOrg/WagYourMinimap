package xyz.wagyourtail.minimap.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import xyz.wagyourtail.minimap.ModLoaderSpecific;

public class ModLoaderSpecificImpl implements ModLoaderSpecific {

    public void checkEnableStencil() {
        Minecraft.getInstance().getMainRenderTarget().enableStencil();
    }

    @Override
    public void clientCommandContextLog(SharedSuggestionProvider p, Component s) {
        throw new UnsupportedOperationException("I haven't implemented client commands on forge < 1.18");
    }

}
