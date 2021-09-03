package xyz.wagyourtail.minimap.api.client;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import xyz.wagyourtail.minimap.client.gui.screen.widget.MenuButton;

import java.util.List;

public class MinimapClientEvents {
    public static final Event<EventMenuButtons> EVENT_MENU_BUTTONS = EventFactory.createLoop();


    public interface EventMenuButtons {
        void onPopulate(List<MenuButton> buttons);
    }
}
