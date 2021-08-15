package xyz.wagyourtail.minimap.api.server;

import xyz.wagyourtail.minimap.api.MinimapApi;

public class MinimapServerApi extends MinimapApi {


    public static MinimapServerApi getInstance() {
        if (INSTANCE == null) INSTANCE = new MinimapServerApi();
        return (MinimapServerApi) INSTANCE;
    }

}
