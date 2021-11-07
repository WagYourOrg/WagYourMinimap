package xyz.wagyourtail.minimap.fabric;

import net.minecraft.client.Minecraft;
import xyz.wagyourtail.minimap.client.ModloaderSpecific;

public class FabricSpecific extends ModloaderSpecific {
    Minecraft mc = Minecraft.getInstance();

    @Override
    public void checkEnableStencil() {
        ((IRenderTarget) mc.getMainRenderTarget()).enableStencil();
    }

}
