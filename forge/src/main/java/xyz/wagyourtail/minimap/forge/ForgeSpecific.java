package xyz.wagyourtail.minimap.forge;

import net.minecraft.client.Minecraft;
import xyz.wagyourtail.minimap.client.ModloaderSpecific;

public class ForgeSpecific extends ModloaderSpecific {
    Minecraft mc = Minecraft.getInstance();

    @Override
    public void checkEnableStencil() {
        mc.getMainRenderTarget().enableStencil();
    }

}
