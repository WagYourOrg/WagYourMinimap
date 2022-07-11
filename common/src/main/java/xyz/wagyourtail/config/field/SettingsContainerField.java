package xyz.wagyourtail.config.field;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class SettingsContainerField<T> implements SettingField.SupplierThrows<T> {
    public final Field field;
    public final Class<T> type;
    private final SettingField.SupplierThrows<T> supplier;

    public SettingsContainerField(Field field, SettingField.SupplierThrows<T> supplier) {
        this.field = field;
        this.type = (Class<T>) field.getType();
        this.supplier = supplier;
    }

    @Override
    public T get() throws InvocationTargetException, IllegalAccessException {
        return supplier.get();
    }

}
