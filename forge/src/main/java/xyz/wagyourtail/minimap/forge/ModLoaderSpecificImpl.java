package xyz.wagyourtail.minimap.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.ClientCommandSourceStack;
import net.minecraftforge.common.extensions.IForgeCommandSourceStack;
import xyz.wagyourtail.minimap.ModLoaderSpecific;

public class ModLoaderSpecificImpl implements ModLoaderSpecific {

    public void checkEnableStencil() {
        Minecraft.getInstance().getMainRenderTarget().enableStencil();
    }

    @Override
    public void clientCommandContextLog(SharedSuggestionProvider p, Component s) {
        ((ClientCommandSourceStack) p).sendSuccess(() -> s, true);
    }

}
