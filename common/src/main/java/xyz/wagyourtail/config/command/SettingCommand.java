package xyz.wagyourtail.config.command;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import xyz.wagyourtail.config.ConfigManager;
import xyz.wagyourtail.config.Or;
import xyz.wagyourtail.config.field.SettingField;
import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.config.field.SettingsContainerField;
import xyz.wagyourtail.minimap.ModLoaderSpecific;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.BiFunction;

@SuppressWarnings({"unchecked", "generic"})
public class SettingCommand<S extends SharedSuggestionProvider> {
    public final ConfigManager configManager;

    public SettingCommand(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public LiteralArgumentBuilder<S> getCommandTree(String baseCommand) throws NoSuchMethodException {
        LiteralArgumentBuilder<S> root = LiteralArgumentBuilder.literal(baseCommand);
        for (Class<?> config : configManager.getRegisteredConfigs()) {
            root.then(getCommandTree(config));
        }
        return root;
    }

    public ArgumentBuilder<S, ?> getCommandTree(Class<?> config) throws NoSuchMethodException {
        SettingsContainer s = config.getAnnotation(SettingsContainer.class);
        ArgumentBuilder<S, ?> root = LiteralArgumentBuilder.literal(translationKeyToCommand(s.value()));
        List<Or<SettingField<?>, SettingsContainerField<Object>>> fields = SettingField.getSettingsFor(configManager, config, () -> configManager.get(config));
        for (Or<SettingField<?>, SettingsContainerField<Object>> field : fields) {
            try {
                if (field.t() != null) {
                        root.then(getCommandTree(field.t(), () -> {
                        }));
                } else if (field.u() != null) {
                    root.then(LiteralArgumentBuilder.<S>literal(translationKeyToCommand(field.u().get().getClass().getAnnotation(SettingsContainer.class).value()))
                        .then(getSubCommandBuilder(field.u().type, field.u(), () -> {})));
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

    public ArgumentBuilder<S, ?> getCommandTree(SettingField<?> field, Runnable preExecute) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, NoSuchFieldException, InstantiationException {
        ArgumentBuilder<S, ?> fieldArg = LiteralArgumentBuilder.literal(translationKeyToCommand(field.setting.value()));
        if (field.fieldType.isArray()) {
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

    public <T> void getCommandTreeForArray(SettingField<T[]> settingField, ArgumentBuilder<S, ?> fieldArg, Runnable preExecute) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, NoSuchFieldException, InstantiationException {
        if (settingField.brigadierOptionsOverride != null) {
            Class<? extends ArgumentType> brigadierType = settingField.brigadierOptionsOverride.value();
            String init = settingField.brigadierOptionsOverride.constructor();
            ArgumentType t;
            if (!Objects.equals(init, "<init>")) {
                t = (ArgumentType) brigadierType.getDeclaredField(init).get(null);
            } else {
                t = brigadierType.getDeclaredConstructor().newInstance();
            }
            Method m = null;
            for (String getter : settingField.brigadierOptionsOverride.getter()) {
                try {
                    m = brigadierType.getMethod(getter, CommandContext.class, String.class);
                } catch (NoSuchMethodException ignored) {}
            }
            if (m == null) throw new NoSuchMethodException("No getter found: " + settingField.getRawField());
            Method finalM = m;
            fieldArg.then(RequiredArgumentBuilder.argument("remove", t).executes(
                ctx -> {
                    preExecute.run();
                    try {
                        Object o = finalM.invoke(null, ctx, "remove");
                        List<T> list = new ArrayList<>(Arrays.asList(settingField.get()));
                        if (settingField.fieldType.getComponentType().equals(String.class)) {
                            list.remove(o.toString());
                        } else {
                            list.remove(o);
                        }
                        settingField.set(list.toArray((T[]) Array.newInstance(settingField.fieldType.getComponentType(), list.size())));
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    return 1;
                }
            ));
            fieldArg.then(RequiredArgumentBuilder.argument("add", t).executes(
                ctx -> {
                    preExecute.run();
                    try {
                        Object o = finalM.invoke(null, ctx, "add");
                        List<T> list = new ArrayList<>(Arrays.asList(settingField.get()));
                        if (settingField.fieldType.getComponentType().equals(String.class)) {
                            list.add((T) o.toString());
                        } else {
                            list.add((T) o);
                        }
                        settingField.set(list.toArray((T[]) Array.newInstance(settingField.fieldType.getComponentType(), list.size())));
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    return 1;
                }
            ));
        } else {
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
                    ModLoaderSpecific.INSTANCE.clientCommandContextLog(ctx.getSource(), component);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                return 1;
            });
            if (options != null) {
                if (settingField.setting.allowDuplicateOption()) {
                    throw new UnsupportedOperationException("Duplicate option not supported yet");
                } else {
                    ArgumentBuilder<S, ?> enable = LiteralArgumentBuilder.literal("add");
                    ArgumentBuilder<S, ?> disable = LiteralArgumentBuilder.literal("remove");
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
                    //TODO: enabled/disabled
                }
            } else {
                throw new UnsupportedOperationException("no options not supported yet");
            }
        }
    }

    public void getCommandTreeForSingle(SettingField<?> settingField, ArgumentBuilder<S, ?> fieldArg, Runnable preExecute) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        ArgumentBuilder<S, ?> set;
        fieldArg.executes(ctx -> {
            preExecute.run();
            try {
                if (settingField.setting.elementType().isAnnotationPresent(SettingsContainer.class)) {
                    ModLoaderSpecific.INSTANCE.clientCommandContextLog(ctx.getSource(), new TextComponent("Current Setting: ").append(new TranslatableComponent(settingField.setting.elementType().getAnnotation(SettingsContainer.class).value())));
                } else {
                    ModLoaderSpecific.INSTANCE.clientCommandContextLog(ctx.getSource(), new TextComponent("Current Setting: ").append(settingField.get().toString()));
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
                ModLoaderSpecific.INSTANCE.clientCommandContextLog(ctx.getSource(), new TextComponent("Set " + settingField.setting.value() + " to " + getter.apply(ctx, "value")));
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
                                    ModLoaderSpecific.INSTANCE.clientCommandContextLog((SharedSuggestionProvider) ctx.getSource(), new TextComponent("Set " + settingField.setting.value() + " to " + obj.get()));
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
                                ModLoaderSpecific.INSTANCE.clientCommandContextLog(ctx.getSource(), new TextComponent("Set " + settingField.setting.value() +
                                    " to ").append(new TranslatableComponent(((Class<?>) option).getAnnotation(
                                    SettingsContainer.class).value())));
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

    public ArgumentBuilder<S, ?> getSubCommandBuilder(Class<?> setting, SettingField.SupplierThrows parentGetter, Runnable preExecute) throws NoSuchMethodException {
        SettingsContainer s = setting.getAnnotation(SettingsContainer.class);
        ArgumentBuilder<S, ?> root = LiteralArgumentBuilder.literal(translationKeyToCommand(s.value()));
        List<Or<SettingField<?>, SettingsContainerField<Object>>> fields = SettingField.getSettingsFor(configManager, setting, parentGetter);
        for (Or<SettingField<?>, SettingsContainerField<Object>> field : fields) {
            try {
                if (field.t() != null) {
                    root.then(getCommandTree(field.t(), preExecute));
                } else if (field.u() != null) {
                    root.then(LiteralArgumentBuilder.<S>literal(translationKeyToCommand(field.u().type.getAnnotation(SettingsContainer.class).value()))
                        .then(getSubCommandBuilder(field.u().type, field.u(), () -> {})));
                }
            } catch (UnsupportedOperationException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                System.err.println("Error while creating command tree for " + setting.getName());
                if (field.t() != null) {
                    System.err.println("Field: " + field.t().getRawField());
                } else if (field.u() != null) {
                    System.err.println("Field: " + field.u().field);
                }
                e.printStackTrace();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return root;
    }

}
