package xyz.wagyourtail.wagyourconfig.gui.widgets;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class Slider extends AbstractSliderButton {
    private final Component title;
    private final Consumer<Double> onChange;
    private final double max, min;
    private final int steps;

    public Slider(int i, int j, int k, int l, Component component, double d, double max, double min, int steps, Consumer<Double> onChange) {
        super(i, j, k, l, component, (d - min) / (max - min));
        this.title = component;
        this.onChange = onChange;
        this.max = max;
        this.min = min;
        this.steps = steps;
    }

    @Override
    protected void updateMessage() {
        this.setMessage(title.copy().append(" " + (Math.floor(value * steps) * (max - min) / steps + min)));
    }

    @Override
    protected void applyValue() {
        onChange.accept(Math.floor(value * steps) * (max - min) / steps + min);
    }

}
