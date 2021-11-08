package xyz.wagyourtail.minimap.fabric;

import net.minecraft.client.Minecraft;

public class ModLoaderSpecificImpl {

    public static void checkEnableStencil() {
        ((IRenderTarget) Minecraft.getInstance().getMainRenderTarget()).enableStencil();
    }

}
