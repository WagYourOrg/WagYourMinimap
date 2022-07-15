package xyz.wagyourtail.minimap;

import java.util.ServiceLoader;

public interface ModLoaderSpecific {
    ModLoaderSpecific INSTANCE =  ServiceLoader.load(ModLoaderSpecific.class).findFirst().orElseThrow(() -> new IllegalStateException("No implementation of ModLoaderSpecific found"));

    /**
     * enables the stencil buffer
     *
     */
    void checkEnableStencil();

}
