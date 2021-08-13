package xyz.wagyourtail.oldminimap.server;

import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import xyz.wagyourtail.oldminimap.WagYourMinimap;

public class WagYourMinimapServer extends WagYourMinimap<WagYourMinimapServerConfig> {
    public static void init() {
        INSTANCE = new WagYourMinimapServer();
    }

    public WagYourMinimapServer() {
        super(WagYourMinimapServerConfig.class);
    }

    @Override
    public String getLevelName(@Nullable Level level) {
        return level.dimension().location().toString().replace(":", "_");
    }

}
