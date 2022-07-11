package xyz.wagyourtail.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import xyz.wagyourtail.config.field.Setting;
import xyz.wagyourtail.config.field.SettingField;
import xyz.wagyourtail.config.field.SettingsContainer;
import xyz.wagyourtail.config.field.SettingsContainerField;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 *
 */
public class SettingContainerSerializer {

    public static JsonObject serialize(Object settingContainer) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        JsonObject serializedSettings = new JsonObject();
        List<Or<SettingField<?>, SettingsContainerField<Object>>> fields = SettingField.getSettingForSkipInsert(
            settingContainer.getClass(),
            () -> settingContainer
        );
        for (Or<SettingField<?>, SettingsContainerField<Object>> field : fields) {
            if (field.t() != null) {
                serializedSettings.add(
                    field.t().getRawField().getName(),
                    serializeSettingsField(
                        field.t().setting.useFunctionsToSerialize() ?
                            field.t().get() :
                            field.t().getRawField().get(settingContainer)
                    )
                );
            } else if (field.u() != null) {
                serializedSettings.add(
                    field.u().field.getName(),
                    serialize(field.u().get())
                );
            }
        }
        return serializedSettings;
    }

    private static JsonElement serializeSettingsField(Object settingValue) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (settingValue.getClass().equals(Character.class)) {
            return new JsonPrimitive((Character) settingValue);
        } else if (settingValue.getClass().equals(Boolean.class)) {
            return new JsonPrimitive((Boolean) settingValue);
        } else if (settingValue.getClass().equals(String.class)) {
            return new JsonPrimitive((String) settingValue);
        } else if (Number.class.isAssignableFrom(settingValue.getClass())) {
            return new JsonPrimitive((Number) settingValue);
        } else if (settingValue.getClass().isEnum()) {
            return new JsonPrimitive(settingValue.toString());
        } else if (settingValue.getClass().isArray()) {
            JsonArray arr = new JsonArray();
            for (int i = 0; i < Array.getLength(settingValue); ++i) {
                arr.add(serializeSettingsField(Array.get(settingValue, i)));
            }
            return arr;
        } else if (Map.class.isAssignableFrom(settingValue.getClass())) {
            JsonObject obj = new JsonObject();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) settingValue).entrySet()) {
                obj.add(entry.getKey().toString(), serializeSettingsField(entry.getValue()));
            }
            return obj;
        } else if (Collection.class.isAssignableFrom(settingValue.getClass())) {
            JsonArray arr = new JsonArray();
            for (Object o : ((Collection<?>) settingValue)) {
                arr.add(serializeSettingsField(o));
            }
            return arr;
        } else {
            JsonObject classContainer = new JsonObject();
            classContainer.addProperty("type", settingValue.getClass().getCanonicalName());
            classContainer.add("value", serialize(settingValue));
            return classContainer;
        }
    }

    public static <T> T deserialze(JsonObject obj, Class<T> settingsContainer) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        T settings = settingsContainer.getConstructor().newInstance();
        deserializeInternal(obj, settings);
        return settings;
    }

    private static <T> void deserializeInternal(JsonObject obj, T settingsContainer) throws NoSuchMethodException {
        List<Or<SettingField<?>, SettingsContainerField<Object>>> fields = SettingField.getSettingForSkipInsert(settingsContainer.getClass(), () -> settingsContainer);
        for (Or<SettingField<?>, SettingsContainerField<Object>> field : fields) {
            try {
                if (field.t() != null) {
                    deserializeField(
                        obj.get(field.t().getRawField().getName()),
                        field.t()
                    );
                } else if (field.u() != null) {
                    deserializeInternal(obj.get(field.u().field.getName()).getAsJsonObject(), field.u().get());
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    /**
     * @param element to serialize
     * @param field field with settings/getter/setter and other stuff (might actually not be type parameter, but
     *     it's ok at runtime)
     * @param <T> type of field
     *
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private static <T> void deserializeField(JsonElement element, SettingField<T> field) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<T> fieldClass = field.setting.useFunctionsToSerialize() ?
            field.fieldType :
            (Class<T>) field.getRawField().getType();
        if (fieldClass.equals(char.class) || fieldClass.equals(Character.class)) {
            setField(field, (T) (Character) element.getAsCharacter());
        } else if (fieldClass.equals(boolean.class) || fieldClass.equals(Boolean.class)) {
            setField(field, (T) (Boolean) element.getAsBoolean());
        } else if (fieldClass.equals(String.class)) {
            setField(field, (T) element.getAsString());
        } else if (fieldClass.isPrimitive() || Number.class.isAssignableFrom(fieldClass)) {
            setField(field, (T) castNumber((Class<Number>) fieldClass, element.getAsNumber()));
        } else if (fieldClass.isEnum()) {
            setField(field, (T) Enum.valueOf((Class<Enum>) fieldClass, element.getAsString()));
        } else if (fieldClass.isArray()) {
            JsonArray arr = element.getAsJsonArray();
            Class<?> arrElementClass = fieldClass.componentType();
            List<Object> array = new ArrayList<>();
            for (JsonElement e : arr) {
                try {
                    array.add(deserializeArrayField(e, arrElementClass, field));
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }
            setField(field, (T) array.toArray((Object[]) Array.newInstance(arrElementClass, 0)));
        } else if (Map.class.isAssignableFrom(fieldClass)) {
            JsonObject map = element.getAsJsonObject();
            Map<String, Object> mapObj =
                field.setting.overrideType() != void.class && Map.class.isAssignableFrom(field.setting.overrideType()) ?
                    (Map<String, Object>) field.setting.overrideType().getConstructor().newInstance() :
                    new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : map.entrySet()) {
                try {
                    mapObj.put(
                        entry.getKey(),
                        deserializeArrayField(entry.getValue(), field.setting.elementType(), field)
                    );
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        } else if (Collection.class.isAssignableFrom(fieldClass)) {
            JsonArray arr = element.getAsJsonArray();
            if (Set.class.isAssignableFrom(fieldClass)) {
                Set<Object> set = field.setting.overrideType() != void.class &&
                    Set.class.isAssignableFrom(field.setting.overrideType()) ?
                    (Set<Object>) field.setting.overrideType().getConstructor().newInstance() :
                    new HashSet<>();
                for (JsonElement jsonElement : arr) {
                    try {
                        set.add(deserializeArrayField(jsonElement, field.setting.elementType(), field));
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            } else if (List.class.isAssignableFrom(fieldClass)) {
                List<Object> list = field.setting.overrideType() != void.class &&
                    List.class.isAssignableFrom(field.setting.overrideType()) ?
                    (List<Object>) field.setting.overrideType().getConstructor().newInstance() :
                    new ArrayList<>();
                for (JsonElement jsonElement : arr) {
                    try {
                        list.add(deserializeArrayField(jsonElement, field.setting.elementType(), field));
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            } else {
                throw new IllegalArgumentException("Collection type not supported: " + fieldClass.getName());
            }
        } else {
            JsonObject obj = element.getAsJsonObject();
            T objContainer = (T) field.construct(Class.forName(obj.get("type").getAsString()));
            setField(field, objContainer);
            deserializeInternal(obj.getAsJsonObject("value"), objContainer);
        }
    }

    private static <T> void setField(SettingField<T> field, T value) throws IllegalAccessException, InvocationTargetException {
        if (field.setting.useFunctionsToSerialize()) {
            field.set(value);
        } else {
            field.getRawField().set(field.getRawParent(), value);
        }
    }

    private static <T> T deserializeArrayField(JsonElement element, Class<T> fieldClass, SettingField<?> field) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (fieldClass.equals(char.class) || fieldClass.equals(Character.class)) {
            return (T) (Character) element.getAsCharacter();
        } else if (fieldClass.equals(boolean.class) || fieldClass.equals(Boolean.class)) {
            return (T) (Boolean) element.getAsBoolean();
        } else if (fieldClass.equals(String.class)) {
            return (T) element.getAsString();
        } else if (fieldClass.isPrimitive() || Number.class.isAssignableFrom(fieldClass)) {
            return (T) castNumber((Class<Number>) fieldClass, element.getAsNumber());
        } else if (fieldClass.isEnum()) {
            return (T) Enum.valueOf((Class<Enum>) fieldClass, element.getAsString());
        } else if (fieldClass.isArray()) {
            JsonArray arr = element.getAsJsonArray();
            Class<?> arrElementClass = fieldClass.componentType();
            List<Object> array = new ArrayList<>();
            for (int i = 0; i < arr.size(); ++i) {
                try {
                    array.add(deserializeArrayField(arr.get(i), arrElementClass, field));
                } catch (ClassNotFoundException ignored) {
                }
            }
            return (T) array.toArray((Object[]) Array.newInstance(arrElementClass, 0));
        } else {
            JsonObject obj = element.getAsJsonObject();
            T instance = (T) field.construct(Class.forName(obj.get("type").getAsString()));
            deserializeInternal(obj.getAsJsonObject("value"), instance);
            return instance;
        }
    }

    public static <T extends Number> T castNumber(Class<T> numberClass, Number number) {
        if (numberClass.equals(byte.class) || numberClass.equals(Byte.class)) {
            return (T) (Byte) number.byteValue();
        } else if (numberClass.equals(short.class) || numberClass.equals(Short.class)) {
            return (T) (Short) number.shortValue();
        } else if (numberClass.equals(int.class) || numberClass.equals(Integer.class)) {
            return (T) (Integer) number.intValue();
        } else if (numberClass.equals(long.class) || numberClass.equals(Long.class)) {
            return (T) (Long) number.longValue();
        } else if (numberClass.equals(float.class) || numberClass.equals(Float.class)) {
            return (T) (Float) number.floatValue();
        } else if (numberClass.equals(double.class) || numberClass.equals(Double.class)) {
            return (T) (Double) number.doubleValue();
        }
        return (T) number;
    }

}
