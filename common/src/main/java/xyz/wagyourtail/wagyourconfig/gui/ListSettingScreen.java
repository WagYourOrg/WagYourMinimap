package xyz.wagyourtail.wagyourconfig.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import xyz.wagyourtail.wagyourconfig.field.SettingField;

public class ListSettingScreen<T> extends Screen {
    private final Screen parent;
    private final SettingField<T[]> container;

    protected ListSettingScreen(Component title, Screen parent, SettingField<T[]> container) {
        super(title);
        this.parent = parent;
        this.container = container;
    }

    @Override
    protected void init() {
        super.init();

    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

}
