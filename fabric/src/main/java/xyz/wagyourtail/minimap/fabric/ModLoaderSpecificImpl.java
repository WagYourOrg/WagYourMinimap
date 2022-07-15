package xyz.wagyourtail.minimap.fabric;

import net.minecraft.client.Minecraft;
import xyz.wagyourtail.minimap.ModLoaderSpecific;

public class ModLoaderSpecificImpl implements ModLoaderSpecific {

    public void checkEnableStencil() {
        ((IRenderTarget) Minecraft.getInstance().getMainRenderTarget()).enableStencil();
    }

}
