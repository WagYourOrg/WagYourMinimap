package xyz.wagyourtail.minimap.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import xyz.wagyourtail.oldminimap.WagYourMinimap;
import xyz.wagyourtail.oldminimap.client.WagYourMinimapClientConfig;

public class ModMenuConfig implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ((WagYourMinimapClientConfig) WagYourMinimap.INSTANCE.config)::getConfigScreen;
    }

}
