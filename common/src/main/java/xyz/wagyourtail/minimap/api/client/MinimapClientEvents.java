package xyz.wagyourtail.minimap.api.client;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import xyz.wagyourtail.minimap.client.gui.screen.widget.InteractMenu;
import xyz.wagyourtail.minimap.client.gui.screen.widget.MenuButton;

import java.util.List;

public class MinimapClientEvents {
    public static final Event<FullscreenMenu> FULLSCREEN_MENU = EventFactory.createLoop();

    public static final Event<FullscreenInteractMenu> FULLSCREEN_INTERACT_MENU = EventFactory.createLoop();

    public interface FullscreenInteractMenu {
        void onPopulate(InteractMenu menu);
    }

    public interface FullscreenMenu {
        void onPopulate(List<MenuButton> buttons);
    }

}
