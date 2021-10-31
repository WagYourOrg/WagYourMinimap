package xyz.wagyourtail.config.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import xyz.wagyourtail.config.field.Setting;
import xyz.wagyourtail.config.field.SettingField;
import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.config.gui.widgets.Checkbox;
import xyz.wagyourtail.config.gui.widgets.NamedEditBox;
import xyz.wagyourtail.config.gui.widgets.Slider;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SettingScreen extends Screen {
    private final Screen parent;
    private final Object settingContainer;
    private final Field[] settings;
    private final List<AbstractWidget> pageButtons = new ArrayList<>();
    private final List<Runnable> enabledListeners = new ArrayList<>();
    private Button backButton;
    private Button forwardButton;

    public SettingScreen(Component title, Screen parent, Object container) {
        super(title);
        this.parent = parent;
        this.settingContainer = container;
        settings = Arrays.stream(settingContainer.getClass().getFields()).filter(e -> e.isAnnotationPresent(Setting.class) || (Modifier.isFinal(e.getModifiers()) && e.getType().isAnnotationPresent(SettingsContainer.class))).toArray(Field[]::new);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 17, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    protected void init() {
        super.init();
        AtomicInteger currentPage = new AtomicInteger();

        backButton = addRenderableWidget(new Button(this.width / 2 - 210, this.height - 30, 100, 20, new TranslatableComponent("gui.wagyourconfig.back"), (btn) -> {
            drawPage(currentPage.decrementAndGet());
        }));

        forwardButton = addRenderableWidget(new Button(this.width / 2 - 105, this.height - 30, 100, 20, new TranslatableComponent("gui.wagyourconfig.forward"), (btn) -> {
            drawPage(currentPage.incrementAndGet());
        }));

        drawPage(0);
    }

    public void drawPage(int page) {
        pageButtons.forEach(this::removeWidget);
        pageButtons.clear();
        enabledListeners.clear();
        int height = this.height - 50 - 30;
        int buttonsPerPage = height / 30 * 2;
        int pages = settings.length / buttonsPerPage;
        int start = Mth.clamp(page, 0, pages) * buttonsPerPage;
        for (int i = start; i < start + buttonsPerPage && i < settings.length; ++i) {
            int finalI = i;
            if (i % 2 == 0)
                for (AbstractWidget abstractWidget : compileSetting(this.width / 2 - 210, 50 + (i / 2) * 30, 205, 20, settings[i])) {
                    pageButtons.add(addRenderableWidget(abstractWidget));
                }
            else
                for (AbstractWidget abstractWidget : compileSetting(this.width / 2 + 5, 50 + (i / 2) * 30, 205, 20, settings[i])) {
                    pageButtons.add(addRenderableWidget(abstractWidget));
                }
        }
        backButton.active = page != 0;
        forwardButton.active = page < pages;
    }

    public AbstractWidget[] compileSetting(int x, int y, int width, int height, Field setting) {
        // pure subsetting
        if (Modifier.isFinal(setting.getModifiers()) && setting.getType().isAnnotationPresent(SettingsContainer.class))
            return new AbstractWidget[] {
                new Button(x, y, width, height, new TranslatableComponent(setting.getType().getAnnotation(SettingsContainer.class).value()), (btn) -> {
                    try {
                        minecraft.setScreen(new SettingScreen(new TranslatableComponent(setting.getType().getAnnotation(SettingsContainer.class).value()), this, setting.get(settingContainer)));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                })
            };
        try {
            AbstractWidget element;
            AbstractWidget settingButton = null;
            SettingField<?> settingField = new SettingField<>(settingContainer, setting, setting.getType());

            //boolean
            if (settingField.fieldType.equals(boolean.class) || settingField.fieldType.equals(Boolean.class)) {
                element = new Checkbox(x, y, width, height, new TranslatableComponent(settingField.setting.value()), (boolean) settingField.get(), true, (bl) -> {
                    try {
                        ((SettingField) settingField).set(bl);
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
            } else

                //char
                if (settingField.fieldType.equals(char.class) || settingField.fieldType.equals(Character.class)) {
                    throw new RuntimeException("CHAR NOT IMPLEMENTED YET!");
                } else

                    //number
                    if (settingField.fieldType.isPrimitive() || Number.class.isAssignableFrom(settingField.fieldType)) {
                        if (settingField.intRange != null) {
                            element = new Slider(x, y, width, height, new TranslatableComponent(settingField.setting.value()), ((Number) settingField.get()).doubleValue(), settingField.intRange.from(), settingField.intRange.to(), settingField.intRange.stepVal() * (settingField.intRange.to() - settingField.intRange.from()), (val) -> {
                                try {
                                    ((SettingField) settingField).set(val.intValue());
                                } catch (InvocationTargetException | IllegalAccessException e) {
                                    e.printStackTrace();
                                }
                            });
                        } else if (settingField.doubleRange != null) {
                            element = new Slider(x, y, width, height, new TranslatableComponent(settingField.setting.value()), ((Number) settingField.get()).doubleValue(), settingField.doubleRange.from(), settingField.doubleRange.to(), settingField.doubleRange.steps(), (val) -> {
                                try {
                                    ((SettingField) settingField).set(val);
                                } catch (InvocationTargetException | IllegalAccessException e) {
                                    e.printStackTrace();
                                }
                            });
                        } else {
                            element = new NamedEditBox(font, x, y, width, height, new TranslatableComponent(settingField.setting.value()));
                            ((EditBox) element).setResponder((val) -> {
                                if (settingField.fieldType.equals(int.class) || settingField.fieldType.equals(Integer.class)) {
                                    try {
                                        ((SettingField) settingField).set(val.equals("") ? 0 : Integer.valueOf(val));
                                    } catch (InvocationTargetException | IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                } else if (settingField.fieldType.equals(double.class) || settingField.fieldType.equals(Double.class)) {
                                    try {
                                        ((SettingField) settingField).set(val.equals("") ? 0D : Double.valueOf(val));
                                    } catch (InvocationTargetException | IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    throw new RuntimeException("Number type " + settingField.fieldType + " not implemented");
                                }
                            });
                            if (settingField.fieldType.equals(int.class) || settingField.fieldType.equals(Integer.class)) {
                                ((EditBox) element).setFilter((str) ->
                                    str.matches("-?\\d*")
                                );
                            } else if (settingField.fieldType.equals(double.class) || settingField.fieldType.equals(Double.class)) {

                                ((EditBox) element).setFilter((str) ->
                                    str.matches("-?\\d*.?\\d*")
                                );
                            } else {
                                throw new RuntimeException("Number type " + settingField.fieldType + " not implemented");
                            }
                        }
                    } else

                        //string
                        if (settingField.fieldType.equals(String.class)) {
                            if (settingField.options() != null) {
                                MutableComponent title = new TranslatableComponent(settingField.setting.value());
                                List<String> settings = (List<String>) settingField.options().stream().toList();
                                element = new Button(x, y, width, height, title.copy().append(" " + settingField.get()), (btn) -> {
                                    try {
                                        ((SettingField) settingField).set(settings.get((settings.indexOf(settingField.get()) + 1) % settings.size()));
                                        btn.setMessage(title.copy().append(" " + settingField.get()));
                                    } catch (InvocationTargetException | IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                });
                            } else {
                                element = new NamedEditBox(font, x, y, width, height, new TranslatableComponent(settingField.setting.value()));
                                ((EditBox) element).setResponder((val) -> {
                                    try {
                                        ((SettingField) settingField).set(val);
                                    } catch (InvocationTargetException | IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                });
                            }
                        } else

                            //enum
                            if (settingField.fieldType.isEnum()) {
                                MutableComponent title = new TranslatableComponent(settingField.setting.value());
                                List<?> settings = settingField.options().stream().toList();
                                element = new Button(x, y, width, height, title.copy().append(" " + settingField.get()), (btn) -> {
                                    try {
                                        ((SettingField) settingField).set(settings.get((settings.indexOf(settingField.get()) + 1) % settings.size()));
                                        btn.setMessage(title.copy().append(" " + settingField.get()));
                                    } catch (InvocationTargetException | IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                });
                            } else

                                //array
                                if (settingField.fieldType.isArray()) {
                                    element = new Button(x, y, width, height, new TranslatableComponent(settingField.setting.value()), (btn) -> {
                                        minecraft.setScreen(new ListScreen<>(new TranslatableComponent(settingField.setting.value()), this, (SettingField<Object[]>) settingField));
                                    });
                                } else

                                //object
                                {
                                    MutableComponent title = new TranslatableComponent(settingField.setting.value());
                                    List<Class<?>> settings = (List<Class<?>>) settingField.options().stream().toList();
                                    element = new Button(x, y, width - height - 5, height, title.copy().append(" ").append(new TranslatableComponent(settingField.get().getClass().getAnnotation(SettingsContainer.class).value())), (btn) -> {
                                        try {
                                            ((SettingField) settingField).set(settings.get((settings.indexOf(settingField.get().getClass()) + 1) % settings.size()).getConstructor().newInstance());
                                            btn.setMessage(title.copy().append(" ").append(new TranslatableComponent(settingField.get().getClass().getAnnotation(SettingsContainer.class).value())));
                                        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException | InstantiationException e) {
                                            e.printStackTrace();
                                        }
                                    });
                                    settingButton = new Button(x + width - height, y, height, height, new TextComponent("⚙"), (btn) -> {
                                        try {
                                            Object settingVal = settingField.get();
                                            minecraft.setScreen(new SettingScreen(new TranslatableComponent(settingVal.getClass().getAnnotation(SettingsContainer.class).value()), this, settingVal));
                                        } catch (InvocationTargetException | IllegalAccessException e) {
                                            e.printStackTrace();
                                        }
                                    });
                                }

            if (!settingField.setting.enabled().equals("")) {
                AbstractWidget finalElement = element;
                AbstractWidget finalSettingButton = settingButton;
                enabledListeners.add(() -> {
                    try {
                        finalElement.active = settingField.enabled();
                        if (finalSettingButton != null) {
                            finalSettingButton.active = finalElement.active;
                        }
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
            }
            if (settingButton == null) {
                return new AbstractWidget[] {element};
            } else {
                return new AbstractWidget[] {element, settingButton};
            }

        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
            ex.printStackTrace();
            return new AbstractWidget[] {};
        }
    }

    @Override
    public void tick() {
        super.tick();
        enabledListeners.forEach(Runnable::run);
    }

}