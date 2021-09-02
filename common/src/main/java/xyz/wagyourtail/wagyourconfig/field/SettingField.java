package xyz.wagyourtail.wagyourconfig.field;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

public class SettingField<T> {
    public final Class<T> fieldType;
    public final Setting setting;
    public final IntRange intRange;
    public final DoubleRange doubleRange;
    private final Object parent;
    private final Field field;
    private final Method enabled;
    private final Method getter;
    private final Method setter;
    private final Method options;

    public SettingField(Object parent, Field field, Class<T> fieldType) throws NoSuchMethodException {
        this.parent = parent;
        this.field = field;
        this.fieldType = fieldType;
        setting = field.getAnnotation(Setting.class);
        intRange = field.getAnnotation(IntRange.class);
        doubleRange = field.getAnnotation(DoubleRange.class);
        if (!setting.enabled().equals(""))
            enabled = parent.getClass().getMethod(setting.enabled());
        else
            enabled = null;
        if (!setting.getter().equals(""))
            getter = parent.getClass().getMethod(setting.getter());
        else
            getter = null;
        if (!setting.setter().equals(""))
            setter = parent.getClass().getMethod(setting.setter(), fieldType);
        else
            setter = null;
        if (!setting.options().equals(""))
            options = parent.getClass().getMethod(setting.options());
        else
            options = null;
    }

    public boolean enabled() throws InvocationTargetException, IllegalAccessException {
        if (enabled != null) return (boolean) enabled.invoke(parent);
        return true;
    }

    public T get() throws InvocationTargetException, IllegalAccessException {
        if (getter != null) return (T) getter.invoke(parent);
        return (T) field.get(parent);
    }

    public void set(T value) throws InvocationTargetException, IllegalAccessException {
        if (setter != null) {
            setter.invoke(parent, value);
        } else {
            field.set(parent, value);
        }
    }

    public Collection<?> options() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (options != null) return (Collection<?>) options.invoke(parent);
        if (fieldType.isEnum()) return Arrays.asList((Object[])fieldType.getDeclaredMethod("values").invoke(null));
        return null;
    }
}
