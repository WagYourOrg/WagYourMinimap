package xyz.wagyourtail.config.field;

import xyz.wagyourtail.config.ConfigManager;
import xyz.wagyourtail.config.Or;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class SettingField<T> {
    private final SupplierThrows<Object> parent;
    private final Field field;
    private final Method enabled;
    private final Method getter;
    private final Method setter;
    private final Method options;
    private final Method constructor;
    public final Class<T> fieldType;
    public final Setting setting;
    public final IntRange intRange;
    public final DoubleRange doubleRange;
    public final InsertInto insertInto;
    public final BrigadierOptionsOverride brigadierOptionsOverride;

    protected SettingField(SupplierThrows<Object> parent, Field field) throws NoSuchMethodException {
        this.parent = parent;
        this.field = field;
        setting = field.getAnnotation(Setting.class);
        this.fieldType = setting.overrideType() != void.class ?
            (Class<T>) setting.overrideType() :
            (Class<T>) field.getType();
        intRange = field.getAnnotation(IntRange.class);
        doubleRange = field.getAnnotation(DoubleRange.class);
        insertInto = field.getAnnotation(InsertInto.class);
        brigadierOptionsOverride = field.getAnnotation(BrigadierOptionsOverride.class);
        if (!setting.enabled().equals("")) {
            enabled = field.getDeclaringClass().getMethod(setting.enabled());
        } else {
            enabled = null;
        }
        if (!setting.getter().equals("")) {
            getter = field.getDeclaringClass().getMethod(setting.getter());
        } else {
            getter = null;
        }
        if (!setting.setter().equals("")) {
            setter = field.getDeclaringClass().getMethod(setting.setter(), fieldType);
        } else {
            setter = null;
        }
        if (!setting.options().equals("")) {
            options = field.getDeclaringClass().getMethod(setting.options());
        } else {
            options = null;
        }
        if (!setting.constructor().equals("")) {
            constructor = field.getDeclaringClass().getMethod(setting.constructor(), Class.class);
        } else {
            constructor = null;
        }
    }

    public static List<Or<SettingField<?>, SettingsContainerField<Object>>> getSettingsFor(ConfigManager config, Class<?> parentClass, SupplierThrows<Object> parentSupplier) throws NoSuchMethodException {
        List<Or<SettingField<?>, SettingsContainerField<Object>>> settings = new ArrayList<>();
        Class<?> finalParentClass = parentClass;
        for (Class<?> registeredConfig : config.getRegisteredConfigs()) {
            Class<?> registeredConfigRealClass = registeredConfig;
            do {
                for (Field field : registeredConfig.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Setting.class) && field.isAnnotationPresent(InsertInto.class)) {
                        if (Arrays.stream(field.getAnnotation(InsertInto.class).value()).anyMatch(e -> e.isAssignableFrom(
                            finalParentClass))) {
                            settings.add(new Or<>(new SettingField<>(() -> config.get(registeredConfigRealClass), field), null));
                        }
                    } else if (Modifier.isFinal(field.getModifiers()) && field.getType().isAnnotationPresent(SettingsContainer.class)) {
                        settings.addAll(getSettingsFor(field.getType(), parentClass, () -> field.get(config.get(registeredConfigRealClass))));
                        if (field.isAnnotationPresent(InsertInto.class)) {
                            if (Arrays.stream(field.getAnnotation(InsertInto.class).value()).anyMatch(e -> e.isAssignableFrom(
                                finalParentClass))) {
                                settings.add(new Or<>(null, new SettingsContainerField<>(field, () -> field.get(config.get(registeredConfigRealClass)))));
                            }
                        }
                    }
                }
            } while ((registeredConfig = registeredConfig.getSuperclass()) != Object.class);
        }
        do {
            for (Field field : parentClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Setting.class)) {
                    settings.add(new Or<>(new SettingField<>(parentSupplier, field), null));
                } else if (Modifier.isFinal(field.getModifiers()) && field.getType().isAnnotationPresent(SettingsContainer.class)) {
                    settings.add(new Or<>(null, new SettingsContainerField<>(field, () -> field.get(parentSupplier.get()))));
                }
            }
        } while ((parentClass = parentClass.getSuperclass()) != Object.class);

        return settings;
    }

    private static List<Or<SettingField<?>, SettingsContainerField<Object>>> getSettingsFor(Class<?> current, Class<?> parentClass, SupplierThrows<Object> currentSupplier) throws NoSuchMethodException {
        List<Or<SettingField<?>, SettingsContainerField<Object>>> settings = new ArrayList<>();
        do {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(Setting.class) && field.isAnnotationPresent(InsertInto.class)) {
                    if (Arrays.stream(field.getAnnotation(InsertInto.class).value()).anyMatch(e -> e.isAssignableFrom(
                        parentClass))) {
                        settings.add(new Or<>(new SettingField<>(currentSupplier, field), null));
                    }
                } else if (Modifier.isFinal(field.getModifiers()) && field.getType().isAnnotationPresent(SettingsContainer.class)) {
                    settings.addAll(getSettingsFor(field.getType(), parentClass, () -> field.get(currentSupplier.get())));
                    if (field.isAnnotationPresent(InsertInto.class)) {
                        if (Arrays.stream(field.getAnnotation(InsertInto.class).value()).anyMatch(e -> e.isAssignableFrom(
                            parentClass))) {
                            settings.add(new Or<>(null, new SettingsContainerField<>(field, () -> field.get(currentSupplier.get()))));
                        }
                    }
                }
            }
        } while ((current = current.getSuperclass()) != Object.class);
        return settings;
    }

    public static List<Or<SettingField<?>, SettingsContainerField<Object>>> getSettingForSkipInsert(Class<?> clazz, SupplierThrows<Object> parent) throws NoSuchMethodException {
        List<Or<SettingField<?>, SettingsContainerField<Object>>> settings = new ArrayList<>();
        Class<?> current = clazz;
        do {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(Setting.class)) {
                    settings.add(new Or<>(new SettingField<>(parent, field), null));
                } else if (Modifier.isFinal(field.getModifiers()) && field.getType().isAnnotationPresent(SettingsContainer.class)) {
                    settings.add(new Or<>(null, new SettingsContainerField<>(field, () -> field.get(parent.get()))));
                }
            }
        } while ((current = current.getSuperclass()) != Object.class);
        return settings;
    }

    public boolean enabled() throws InvocationTargetException, IllegalAccessException {
        if (enabled != null) {
            return (boolean) enabled.invoke(parent.get());
        }
        return true;
    }

    public T get() throws InvocationTargetException, IllegalAccessException {
        if (getter != null) {
            return (T) getter.invoke(parent.get());
        }
        return (T) field.get(parent.get());
    }

    public void set(T value) throws InvocationTargetException, IllegalAccessException {
        if (setter != null) {
            setter.invoke(parent.get(), value);
        } else {
            field.set(parent.get(), value);
        }
    }

    public Collection<?> options() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (options != null) {
            return (Collection<?>) options.invoke(parent.get());
        }
        if (fieldType.isEnum()) {
            return Arrays.asList((Object[]) fieldType.getDeclaredMethod("values").invoke(null));
        }
        return null;
    }

    public <U> U construct(Class<U> clazz) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        if (constructor != null) {
            return (U) constructor.invoke(parent.get(), clazz);
        } else {
            return clazz.getConstructor().newInstance();
        }
    }

    public Object getRawParent() throws InvocationTargetException, IllegalAccessException {
        return parent.get();
    }

    public Field getRawField() {
        return field;
    }


    @FunctionalInterface
    public interface SupplierThrows<T> {
        T get() throws InvocationTargetException, IllegalAccessException;
    }
}
