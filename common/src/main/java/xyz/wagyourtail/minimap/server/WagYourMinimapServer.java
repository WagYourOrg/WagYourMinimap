package xyz.wagyourtail.minimap.server;

import xyz.wagyourtail.minimap.WagYourMinimap;
public class WagYourMinimapServer extends WagYourMinimap<WagYourMinimapServerConfig> {
    public static void init() {
        INSTANCE = new WagYourMinimapServer();
    }

    public WagYourMinimapServer() {
        super(WagYourMinimapServerConfig.class);
    }
}
