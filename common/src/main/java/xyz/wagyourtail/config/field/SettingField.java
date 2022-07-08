package xyz.wagyourtail.config.field;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
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

    public SettingField(SupplierThrows<Object> parent, Field field) throws NoSuchMethodException {
        this.parent = parent;
        this.field = field;
        setting = field.getAnnotation(Setting.class);
        this.fieldType = setting.overrideType() != void.class ?
            (Class<T>) setting.overrideType() :
            (Class<T>) field.getType();
        intRange = field.getAnnotation(IntRange.class);
        doubleRange = field.getAnnotation(DoubleRange.class);
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
