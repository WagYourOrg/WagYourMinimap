package xyz.wagyourtail.minimap.api.client.config;

import xyz.wagyourtail.config.field.Setting;
import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.minimap.api.MinimapApi;

import java.util.HashMap;
import java.util.Map;

@SettingsContainer("gui.wagyourminimap.settings.per_server")
public class CurrentServerConfig {

    /**
     * by setting it this way with overrides we can trick the gui to only show the server we're currently connected to
     * while still serializing the settings for all servers
     *
     * the elementType is required since it's serialized as a Map
     */
    @Setting(value = "gui.wagyourminimap.settings.per_server.tp_command",
        getter = "getTpCommand",
        setter = "setTpCommand",
        useFunctionsToSerialize = false,
        overrideType = String.class,
        elementType = String.class)
    public Map<String, String> tpCommand = new HashMap<>();

    public String getTpCommand() {
        return tpCommand.getOrDefault(MinimapApi.getInstance().getMapServer().server_slug, "/tp %player %x %y %z");
    }

    public void setTpCommand(String value) {
        tpCommand.put(MinimapApi.getInstance().getMapServer().server_slug, value);
    }

}
