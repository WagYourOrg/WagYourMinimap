package xyz.wagyourtail.wagyourconfig.gui.widgets;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.text.DecimalFormat;
import java.util.function.Consumer;

public class Slider extends AbstractSliderButton {
    private final Component title;
    private final Consumer<Double> onChange;
    private final double max, min;
    private final int steps;

    public Slider(int i, int j, int k, int l, Component component, double d, double max, double min, int steps, Consumer<Double> onChange) {
        super(i, j, k, l, component, 1 - (d - min) / (max - min));
        this.title = component;
        this.onChange = onChange;
        this.max = max;
        this.min = min;
        this.steps = steps;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        this.setMessage(title.copy().append(" " + new DecimalFormat().format(Math.floor((1 - value) * steps) * (max - min) / steps + min)));
    }

    @Override
    protected void applyValue() {
        value = Math.floor(value * steps) / steps;
        onChange.accept(Math.floor((1 - value) * steps) * (max - min) / steps + min);
    }

}
