package xyz.wagyourtail.minimap.forge;

import net.minecraft.client.Minecraft;
import xyz.wagyourtail.minimap.ModLoaderSpecific;

public class ModLoaderSpecificImpl extends ModLoaderSpecific {

    public static void checkEnableStencil() {
        Minecraft.getInstance().getMainRenderTarget().enableStencil();
    }

}
