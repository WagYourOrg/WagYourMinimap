package xyz.wagyourtail.config.command;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import xyz.wagyourtail.config.ConfigManager;
import xyz.wagyourtail.config.field.Setting;
import xyz.wagyourtail.config.field.SettingField;
import xyz.wagyourtail.config.field.SettingsContainer;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class SettingCommand<S extends CommandSource> {
    public final ConfigManager configManager;

    public SettingCommand(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public LiteralArgumentBuilder<S> getCommandTree(String baseCommand) {
        LiteralArgumentBuilder<S> root = LiteralArgumentBuilder.literal(baseCommand);
        for (Class<?> config : configManager.getRegisteredConfigs()) {
            root.then(getCommandTree(config));
        }
        return root;
    }

    public ArgumentBuilder<S, ?> getCommandTree(Class<?> config) {
        SettingsContainer s = config.getAnnotation(SettingsContainer.class);
        ArgumentBuilder<S, ?> root = LiteralArgumentBuilder.literal(translationKeyToCommand(s.value()));
        for (Field field : config.getDeclaredFields()) {
            try {
                if (field.isAnnotationPresent(Setting.class)) {
                    root.then(getCommandTree(new SettingField<>(() -> configManager.get(config), field), () -> {
                    }));
                } else if (Modifier.isFinal(field.getModifiers()) &&
                    field.isAnnotationPresent(SettingsContainer.class)) {
                    root.then(LiteralArgumentBuilder.<S>literal(translationKeyToCommand(field.getAnnotation(
                            SettingsContainer.class).value()))
                        .then(getSubCommandBuilder(field.getType(), () -> field.get(configManager.get(config)), () -> {
                        })));
                }
            } catch (UnsupportedOperationException e) {
                e.printStackTrace();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return root;
    }

    private String translationKeyToCommand(String key) {
        String[] parts = key.split("\\.");
        return parts[parts.length - 1];
    }

    public ArgumentBuilder<S, ?> getCommandTree(SettingField<?> field, Runnable preExecute) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        ArgumentBuilder<S, ?> fieldArg = LiteralArgumentBuilder.literal(translationKeyToCommand(field.setting.value()));
        if (field.setting.elementType().isArray()) {
            getCommandTreeForArray((SettingField) field, fieldArg, preExecute);
        } else if (field.setting.elementType().isAssignableFrom(Collection.class)) {
            throw new UnsupportedOperationException("Collection types are not supported yet");
        } else if (field.setting.elementType().isAssignableFrom(Map.class)) {
            throw new UnsupportedOperationException("Map settings are not supported yet");
        } else {
            getCommandTreeForSingle(field, fieldArg, preExecute);
        }
        return fieldArg;
    }

    public <T> void getCommandTreeForArray(SettingField<T[]> settingField, ArgumentBuilder<S, ?> fieldArg, Runnable preExecute) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Collection<?> options = settingField.options();
        fieldArg.executes(ctx -> {
            preExecute.run();
            try {
                MutableComponent component = new TextComponent("Current value: ");
                for (T thing : settingField.get()) {
                    if (thing.getClass().isAnnotationPresent(SettingsContainer.class)) {
                        component.append(new TranslatableComponent(thing.getClass().getAnnotation(SettingsContainer.class).value()));
                    } else {
                        component.append(new TextComponent(thing.toString()));
                    }
                    component.append(", ");
                }
                ctx.getSource().sendMessage(component, null);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            return 1;
        });
        if (options != null) {
            if (settingField.setting.allowDuplicateOption()) {
                throw new UnsupportedOperationException("Duplicate option not supported yet");
            } else {
                ArgumentBuilder<S, ?> enable = LiteralArgumentBuilder.literal("enable");
                ArgumentBuilder<S, ?> disable = LiteralArgumentBuilder.literal("disable");
                ArgumentBuilder<S, ?> subSetting = LiteralArgumentBuilder.literal("subsetting");
                fieldArg.then(enable).then(disable).then(subSetting);
                for (Object option : options) {
                    if (option instanceof Class<?>) {
                        subSetting.then(getSubCommandBuilder((Class<?>) option, () -> {
                            Object arr = settingField.get();
                            for (int i = 0; i < Array.getLength(arr); ++i) {
                                if (Array.get(arr, i).getClass().equals(option)) {
                                    return Array.get(arr, i);
                                }
                            }
                            throw new RuntimeException("Option not found");
                        }, () -> {
                        }));
                    } else {
                        throw new UnsupportedOperationException("non class options are not supported yet");
                    }
                }
            }
        } else {
            throw new UnsupportedOperationException("no options not supported yet");
        }
    }

    public void getCommandTreeForSingle(SettingField<?> settingField, ArgumentBuilder<S, ?> fieldArg, Runnable preExecute) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        ArgumentBuilder<S, ?> set;
        fieldArg.executes(ctx -> {
            preExecute.run();
            try {
                if (settingField.setting.elementType().isAnnotationPresent(SettingsContainer.class)) {
                    ctx.getSource().sendMessage(new TextComponent("Current Setting: ").append(new TranslatableComponent(settingField.setting.elementType().getAnnotation(SettingsContainer.class).value())), null);
                } else {
                    ctx.getSource().sendMessage(new TextComponent("Current Setting: ").append(settingField.get().toString()), null);
                }
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            return 1;
        });
        Collection<?> options = settingField.options();
        if (settingField.fieldType.isPrimitive() || settingField.fieldType.isAssignableFrom(String.class)) {
            BiFunction<CommandContext<S>, String, ?> getter;
            if (options != null) {
                if (settingField.fieldType.equals(String.class)) {
                    set = RequiredArgumentBuilder.argument("value", StringArgumentType.string());
                    getter = StringArgumentType::getString;
                } else {
                    throw new UnsupportedOperationException("options not supported yet");
                }
            } else {
                if (settingField.fieldType.equals(boolean.class) || settingField.fieldType.equals(Boolean.class)) {
                    set = RequiredArgumentBuilder.argument("value", BoolArgumentType.bool());
                    getter = BoolArgumentType::getBool;
                } else if (settingField.fieldType.equals(char.class) ||
                    settingField.fieldType.equals(Character.class)) {
                    throw new UnsupportedOperationException("char settings are not supported yet");
                } else if (settingField.fieldType.equals(int.class) || settingField.fieldType.equals(Integer.class)) {
                    if (settingField.intRange != null) {
                        set = RequiredArgumentBuilder.argument(
                            "value",
                            IntegerArgumentType.integer(settingField.intRange.from(), settingField.intRange.to())
                        );
                    } else {
                        set = RequiredArgumentBuilder.argument("value", IntegerArgumentType.integer());
                    }
                    getter = IntegerArgumentType::getInteger;
                } else if (settingField.fieldType.equals(double.class) || settingField.fieldType.equals(Double.class)) {
                    if (settingField.doubleRange != null) {
                        set = RequiredArgumentBuilder.argument(
                            "value",
                            DoubleArgumentType.doubleArg(settingField.doubleRange.from(), settingField.doubleRange.to())
                        );
                    } else {
                        set = RequiredArgumentBuilder.argument("value", DoubleArgumentType.doubleArg());
                    }
                    getter = DoubleArgumentType::getDouble;
                } else {
                    set = RequiredArgumentBuilder.argument("value", StringArgumentType.string());
                    getter = StringArgumentType::getString;
                }
            }
            set.executes(ctx -> {
                preExecute.run();
                try {
                    ((SettingField) settingField).set(getter.apply(ctx, "value"));
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                ctx.getSource().sendMessage(new TextComponent("Set " + settingField.setting.value() + " to " + getter.apply(ctx, "value")), null);
                return 1;
            });
            fieldArg.then(set);
        } else {
            if (options != null) {
                if (settingField.fieldType.isEnum()) {
                    fieldArg.then(
                        (ArgumentBuilder) RequiredArgumentBuilder.argument("value", StringArgumentType.string())
                            .suggests((s, b) -> SharedSuggestionProvider.suggest(options.stream().map(o -> o.toString()).toArray(String[]::new), b))
                            .executes(ctx -> {
                                Optional<Object> obj = (Optional) options.stream().filter(e -> e.toString().equals(ctx.getArgument("value", String.class))).findFirst();
                                try {
                                    ((SettingField) settingField).set(obj.get());
                                    ((CommandSource) ctx.getSource()).sendMessage(new TextComponent("Set " + settingField.setting.value() + " to " + obj.get()), null);
                                } catch (InvocationTargetException | IllegalAccessException e) {
                                    throw new RuntimeException(e);
                                }
                                return 1;
                            })
                        );
                } else {
                    for (Object option : options) {
                        if (option instanceof Class<?>) {
                            fieldArg.then(getSubCommandBuilder((Class<?>) option, settingField::get, () -> {
                                try {
                                    if (!settingField.get().getClass().equals(option)) {
                                        ((SettingField) settingField).set(settingField.construct((Class<?>) option));
                                    }
                                } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException |
                                         InstantiationException e) {
                                    throw new RuntimeException(e);
                                }
                            }).executes(ctx -> {
                                try {
                                    if (!settingField.get().getClass().equals(option)) {
                                        ((SettingField) settingField).set(settingField.construct((Class<?>) option));
                                    }
                                } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException |
                                         InstantiationException e) {
                                    throw new RuntimeException(e);
                                }
                                ctx.getSource().sendMessage(new TextComponent("Set " + settingField.setting.value() +
                                    " to ").append(new TranslatableComponent(((Class<?>) option).getAnnotation(
                                    SettingsContainer.class).value())), null);
                                return 1;
                            }));
                        } else {
                            throw new UnsupportedOperationException("non class options are not supported yet");
                        }
                    }
                }
            } else {
                throw new UnsupportedOperationException("Unsupported type: " + settingField.fieldType);
            }
        }
    }

    public ArgumentBuilder<S, ?> getSubCommandBuilder(Class<?> setting, SettingField.SupplierThrows parentGetter, Runnable preExecute) {
        SettingsContainer s = setting.getAnnotation(SettingsContainer.class);
        ArgumentBuilder<S, ?> root = LiteralArgumentBuilder.literal(translationKeyToCommand(s.value()));
        for (Field field : setting.getDeclaredFields()) {
            try {
                if (field.isAnnotationPresent(Setting.class)) {
                    root.then(getCommandTree(new SettingField<>(parentGetter, field), preExecute));
                } else if (Modifier.isFinal(field.getModifiers()) &&
                    field.isAnnotationPresent(SettingsContainer.class)) {
                    root.then(LiteralArgumentBuilder.<S>literal(translationKeyToCommand(field.getAnnotation(
                        SettingsContainer.class).value())).then(getSubCommandBuilder(field.getType(), () -> {
                        try {
                            return field.get(parentGetter.get());
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }, preExecute)));
                }
            } catch (UnsupportedOperationException e) {
                e.printStackTrace();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return root;
    }

}
