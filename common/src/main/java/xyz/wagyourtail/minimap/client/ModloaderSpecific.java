package xyz.wagyourtail.minimap.client;

public abstract class ModloaderSpecific {
    public static ModloaderSpecific instance = null;

    public abstract void checkEnableStencil();
}
