package xyz.wagyourtail.config.gui.widgets;

import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class Checkbox extends net.minecraft.client.gui.components.Checkbox {
    public final Consumer<Boolean> onPressed;

    public Checkbox(int i, int j, int k, int l, Component component, boolean bl, Consumer<Boolean> onPressed) {
        super(i, j, k, l, component, bl);
        this.onPressed = onPressed;
    }

    public Checkbox(int i, int j, int k, int l, Component component, boolean bl, boolean bl2, Consumer<Boolean> onPressed) {
        super(i, j, k, l, component, bl, bl2);
        this.onPressed = onPressed;
    }

    @Override
    public void onPress() {
        super.onPress();
        onPressed.accept(this.selected());
    }

}
