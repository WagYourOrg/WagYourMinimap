package xyz.wagyourtail.minimap;

import dev.architectury.injectables.annotations.ExpectPlatform;

public class ModLoaderSpecific {

    /**
     * enables the stencil framebuffer
     */
    @ExpectPlatform
    public static void checkEnableStencil() {
        throw new AssertionError();
    }
}
