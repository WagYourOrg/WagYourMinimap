package xyz.wagyourtail.wagyourconfig.gui;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import xyz.wagyourtail.wagyourconfig.field.SettingField;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class ListSettingScreen<T> extends Screen {
    private final Screen parent;
    private final SettingField<T[]> container;

    private final List<T> enabled;

    private final Class<T> fieldType;

    protected ListSettingScreen(Component title, Screen parent, SettingField<T[]> container) {
        super(title);
        this.parent = parent;
        this.container = container;

        List<T> enabled1;
        try {
            enabled1 = Lists.newArrayList(container.get());
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            enabled1 = new ArrayList<>();
        }
        this.enabled = enabled1;
        this.fieldType = (Class<T>) container.fieldType.componentType();
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
