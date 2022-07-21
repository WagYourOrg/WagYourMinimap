package xyz.wagyourtail.minimap;

import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.ServiceLoader;

public interface ModLoaderSpecific {
    ModLoaderSpecific INSTANCE =  ServiceLoader.load(ModLoaderSpecific.class).findFirst().orElseThrow(() -> new IllegalStateException("No implementation of ModLoaderSpecific found"));

    /**
     * enables the stencil buffer
     *
     */
    void checkEnableStencil();

    void clientCommandContextLog(SharedSuggestionProvider p, Component s);

}
