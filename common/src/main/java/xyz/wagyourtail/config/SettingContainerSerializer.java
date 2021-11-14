package xyz.wagyourtail.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import xyz.wagyourtail.config.field.Setting;
import xyz.wagyourtail.config.field.SettingField;
import xyz.wagyourtail.config.field.SettingsContainer;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class SettingContainerSerializer {

    public static JsonObject serialize(Object settingContainer) throws IllegalAccessException {
        JsonObject serializedSettings = new JsonObject();
        Class<?> settings = settingContainer.getClass();
        Field[] fields = settings.getFields();
        for (Field field : fields) {
            //setting field
            if (field.isAnnotationPresent(Setting.class)) {
                serializedSettings.add(field.getName(), serializeSettingsField(field.get(settingContainer)));
                //pure subsettings class
            } else if (Modifier.isFinal(field.getModifiers()) &&
                field.getType().isAnnotationPresent(SettingsContainer.class)) {
                serializedSettings.add(field.getName(), serialize(field.get(settingContainer)));
            }
        }
        return serializedSettings;
    }

    private static JsonElement serializeSettingsField(Object settingValue) throws IllegalAccessException {
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

    private static <T> void deserializeInternal(JsonObject obj, T settingsContainer) {
        Field[] fields = settingsContainer.getClass().getFields();
        for (Field field : fields) {
            try {
                if (field.isAnnotationPresent(Setting.class)) {
                    if (obj.has(field.getName())) {
                        deserializeField(
                            obj.get(field.getName()),
                            new SettingField<>(settingsContainer, field, (Class<Object>) field.getType())
                        );
                    }
                } else if (Modifier.isFinal(field.getModifiers()) && field.getType().isAnnotationPresent(
                    SettingsContainer.class)) {
                    if (obj.has(field.getName())) {
                        deserializeInternal(obj.get(field.getName()).getAsJsonObject(), field.get(settingsContainer));
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private static <T> void deserializeField(JsonElement element, SettingField<T> field) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<T> fieldClass = field.fieldType;
        if (fieldClass.equals(char.class) || fieldClass.equals(Character.class)) {
            field.set((T) (Character) element.getAsCharacter());
        } else if (fieldClass.equals(boolean.class) || fieldClass.equals(Boolean.class)) {
            field.set((T) (Boolean) element.getAsBoolean());
        } else if (fieldClass.equals(String.class)) {
            field.set((T) element.getAsString());
        } else if (fieldClass.isPrimitive() || Number.class.isAssignableFrom(fieldClass)) {
            field.set((T) castNumber((Class<Number>) fieldClass, element.getAsNumber()));
        } else if (fieldClass.isEnum()) {
            field.set((T) Enum.valueOf((Class<Enum>) fieldClass, element.getAsString()));
        } else if (fieldClass.isArray()) {
            JsonArray arr = element.getAsJsonArray();
            Class<?> arrElementClass = fieldClass.componentType();
            List<Object> array = new ArrayList<>();
            for (int i = 0; i < arr.size(); ++i) {
                try {
                    array.add(deserializeArrayField(arr.get(i), arrElementClass));
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            field.set((T) array.toArray((Object[]) Array.newInstance(arrElementClass, 0)));
        } else {
            JsonObject obj = element.getAsJsonObject();
            Class<?> objClass = Class.forName(obj.get("type").getAsString());
            T objContainer = (T) objClass.getConstructor().newInstance();
            field.set(objContainer);
            deserializeInternal(obj.getAsJsonObject("value"), objContainer);
        }
    }

    private static <T> T deserializeArrayField(JsonElement element, Class<T> fieldClass) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
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
                    array.add(deserializeArrayField(arr.get(i), arrElementClass));
                } catch (ClassNotFoundException ignored) {
                }
            }
            return (T) array.toArray((Object[]) Array.newInstance(arrElementClass, 0));
        } else {
            JsonObject obj = element.getAsJsonObject();
            T instance = (T) Class.forName(obj.get("type").getAsString()).getConstructor().newInstance();
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
