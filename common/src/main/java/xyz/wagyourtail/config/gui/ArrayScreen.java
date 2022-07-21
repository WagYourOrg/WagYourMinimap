package xyz.wagyourtail.config.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import xyz.wagyourtail.config.ConfigManager;
import xyz.wagyourtail.config.field.SettingField;
import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.config.gui.widgets.DisabledSettingList;
import xyz.wagyourtail.config.gui.widgets.EnabledSettingList;
import xyz.wagyourtail.config.gui.widgets.NamedEditBox;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ArrayScreen<T, U> extends Screen implements EnabledSettingList.EntryController<T>, DisabledSettingList.EntryController<U> {
    private final Screen parent;
    private final ConfigManager config;
    private final SettingField<T[]> setting;
    private EnabledSettingList<T> enabledEntries;
    private DisabledSettingList<U> availableEntries;

    private Button doneButton;

    protected ArrayScreen(Component component, Screen parent, ConfigManager config, SettingField<T[]> setting) {
        super(component);
        this.parent = parent;
        this.setting = setting;
        this.config = config;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        this.enabledEntries.render(poseStack, mouseX, mouseY, partialTicks);
        if (this.availableEntries != null) {
            this.availableEntries.render(poseStack, mouseX, mouseY, partialTicks);
        }

        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 17, 0xFFFFFF);

        super.render(poseStack, mouseX, mouseY, partialTicks);

    }

    @Override
    public void onClose() {
        applyValue();
        minecraft.setScreen(parent);
    }

    public void applyValue() {
        T[] arr = enabledEntries.children()
            .stream()
            .map(e -> e.option)
            .toArray((i) -> (T[]) Array.newInstance(setting.fieldType.componentType(), i));
        try {
            setting.set(arr);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void init() {
        super.init();
        this.addWidget(enabledEntries = new EnabledSettingList<>(minecraft, width > 810 ? 400 : 200, this.height, config));
        this.enabledEntries.setLeftPos(this.width / 2 - 4 - this.enabledEntries.getRowWidth());
        try {
            this.enabledEntries.children().addAll(Arrays.stream(setting.get())
                .map(e -> new EnabledSettingList.EnabledSettingEntry<>(
                    minecraft,
                    this,
                    enabledEntries,
                    e,
                    e.getClass().isAnnotationPresent(SettingsContainer.class) ?
                        new TranslatableComponent(e.getClass().getAnnotation(SettingsContainer.class).value()) :
                        new TextComponent(e.toString())
                ))
                .collect(Collectors.toList()));
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }

        try {
            if (setting.fieldType.componentType().equals(char.class) || setting.fieldType.componentType().equals(
                Character.class)) {
                throw new RuntimeException("NON Object List not yet implemented");
            } else if (setting.fieldType.componentType().isPrimitive() ||
                Number.class.isAssignableFrom(setting.fieldType.componentType())) {
                throw new RuntimeException("NON Object List not yet implemented");
            } else if (setting.fieldType.componentType().equals(String.class)) {
                Collection<String> options = (Collection<String>) setting.options();
                if (options != null) {
                    this.addWidget(availableEntries = new DisabledSettingList<>(
                        minecraft,
                        width > 810 ? 400 : 200,
                        this.height
                    ));
                    this.availableEntries.setLeftPos(this.width / 2 + 4);
                    List<String> enabledEntries = this.enabledEntries.children()
                        .stream()
                        .map(e -> (String) e.option)
                        .collect(Collectors.toList());
                    this.availableEntries.children().addAll(options.stream().filter(e -> !enabledEntries.contains(e) ||
                        setting.setting.allowDuplicateOption()).map(e -> new DisabledSettingList.DisabledSettingEntry<>(
                        minecraft,
                        this,
                        availableEntries,
                        (U) e,
                        new TextComponent(e)
                    )).collect(Collectors.toList()));
                } else {
                    NamedEditBox box = this.addRenderableWidget(new NamedEditBox(
                        font,
                        this.width / 2 + 4,
                        this.height / 2 - 20,
                        200,
                        20,
                        new TranslatableComponent("gui.wagyourconfig.addentry")
                    ));
                    this.addRenderableWidget(new Button(
                        this.width / 2 + 4,
                        this.height / 2 + 4,
                        200,
                        20,
                        new TranslatableComponent("gui.wagyourconfig.submit"),
                        (b) -> {
                            if (!setting.setting.allowDuplicateOption() && this.enabledEntries.children()
                                .stream()
                                .anyMatch(e -> e.option.equals(box.getValue()))) {
                                return;
                            }
                            this.enabledEntries.children().add(new EnabledSettingList.EnabledSettingEntry(
                                minecraft,
                                this,
                                enabledEntries,
                                box.getValue(),
                                new TextComponent(box.getValue())
                            ));
                        }
                    ));
                }
            } else if (setting.fieldType.componentType().isEnum()) {
                throw new RuntimeException("NON Object List not yet implemented");
            } else if (setting.fieldType.componentType().isArray()) {
                throw new RuntimeException("NON Object List not yet implemented");
            } else {
                Collection<Class<T>> options = (Collection<Class<T>>) setting.options();
                if (options != null) {
                    this.addWidget(availableEntries = new DisabledSettingList<>(
                        minecraft,
                        width > 810 ? 400 : 200,
                        this.height
                    ));
                    this.availableEntries.setLeftPos(this.width / 2 + 4);
                    List<Class<T>> enabledEntries = this.enabledEntries.children()
                        .stream()
                        .map(e -> (Class<T>) e.option.getClass())
                        .collect(Collectors.toList());
                    this.availableEntries.children().addAll(options.stream().filter(e -> !enabledEntries.contains(e) ||
                        setting.setting.allowDuplicateOption()).map(e -> new DisabledSettingList.DisabledSettingEntry<>(
                        minecraft,
                        this,
                        availableEntries,
                        (U) e,
                        new TranslatableComponent(e.getAnnotation(SettingsContainer.class).value())
                    )).collect(Collectors.toList()));
                } else {
                    throw new RuntimeException("NON Options Object List not yet implemented");
                }
            }
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        doneButton = addRenderableWidget(new Button(
            this.width / 2 + 5,
            this.height - 30,
            200,
            20,
            new TranslatableComponent("gui.wagyourconfig.done"),
            (btn) -> onClose()
        ));
    }

    @Override
    public boolean canMoveUp(EnabledSettingList.EnabledSettingEntry<T> option) {
        return enabledEntries.children().get(0) != option;
    }

    @Override
    public boolean canMoveDown(EnabledSettingList.EnabledSettingEntry<T> option) {
        return enabledEntries.children().get(enabledEntries.children().size() - 1) != option;
    }

    @Override
    public void moveUp(EnabledSettingList.EnabledSettingEntry<T> option) {
        int index = Math.max(1, enabledEntries.children().indexOf(option));
        enabledEntries.children().remove(option);
        enabledEntries.children().add(index - 1, option);
    }

    @Override
    public void moveDown(EnabledSettingList.EnabledSettingEntry<T> option) {
        int index = Math.min(enabledEntries.children().size() - 1, enabledEntries.children().indexOf(option));
        enabledEntries.children().remove(option);
        enabledEntries.children().add(index + 1, option);
    }

    @Override
    public void unselect(EnabledSettingList.EnabledSettingEntry<T> option) {
        enabledEntries.children().remove(option);
        if (!setting.setting.allowDuplicateOption()) {
            try {
                Collection<String> options = (Collection<String>) setting.options();
                if (setting.fieldType.componentType().equals(char.class) || setting.fieldType.componentType().equals(
                    Character.class)) {
                    throw new RuntimeException("NON Object List not yet implemented");
                } else if (setting.fieldType.componentType().isPrimitive() ||
                    Number.class.isAssignableFrom(setting.fieldType.componentType())) {
                    throw new RuntimeException("NON Object List not yet implemented");
                } else if (setting.fieldType.componentType().equals(String.class)) {
                    if (options != null) {
                        throw new RuntimeException("STRING OPTIONS NOT YET IMPLEMENTED");
                    }
                } else if (setting.fieldType.componentType().isEnum()) {
                    throw new RuntimeException("NON Object List not yet implemented");
                } else if (setting.fieldType.componentType().isArray()) {
                    throw new RuntimeException("NON Object List not yet implemented");
                } else {
                    if (options != null) {
                        availableEntries.children().add(new DisabledSettingList.DisabledSettingEntry<>(
                            minecraft,
                            this,
                            availableEntries,
                            (U) option.option.getClass(),
                            new TranslatableComponent(option.option.getClass()
                                .getAnnotation(SettingsContainer.class)
                                .value())
                        ));
                    }
                }
            } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void select(DisabledSettingList.DisabledSettingEntry<U> option) {
        if (!setting.setting.allowDuplicateOption()) {
            availableEntries.children().remove(option);
        }
        try {
            if (setting.fieldType.componentType().equals(char.class) || setting.fieldType.componentType().equals(
                Character.class)) {
                throw new RuntimeException("NON Object List not yet implemented");
            } else if (setting.fieldType.componentType().isPrimitive() ||
                Number.class.isAssignableFrom(setting.fieldType.componentType())) {
                throw new RuntimeException("NON Object List not yet implemented");
            } else if (setting.fieldType.componentType().equals(String.class)) {
                enabledEntries.children().add(new EnabledSettingList.EnabledSettingEntry<>(
                    minecraft,
                    this,
                    enabledEntries,
                    (T) option.option,
                    new TextComponent(option.option.toString())
                ));
            } else if (setting.fieldType.componentType().isEnum()) {
                throw new RuntimeException("NON Object List not yet implemented");
            } else if (setting.fieldType.componentType().isArray()) {
                throw new RuntimeException("NON Object List not yet implemented");
            } else {
                enabledEntries.children().add(new EnabledSettingList.EnabledSettingEntry<T>(
                    minecraft,
                    this,
                    enabledEntries,
                    setting.construct((Class<T>) option.option),
                    new TranslatableComponent(((Class<T>) option.option).getAnnotation(SettingsContainer.class)
                        .value())
                ));
            }
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException |
                 InstantiationException e) {
            e.printStackTrace();
        }
    }

}
