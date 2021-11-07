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
            } else if (Modifier.isFinal(field.getModifiers()) && field.getType().isAnnotationPresent(SettingsContainer.class)) {
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

    public static <T> T deserialize(JsonObject obj, Class<T> settingClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        T settingContainer = settingClass.getConstructor().newInstance();
        deserialize(obj, settingContainer);
        return settingContainer;
    }

    private static <T> void deserialize(JsonObject obj, T settingsContainer) {
        Field[] fields = settingsContainer.getClass().getFields();
        for (Field field : fields) {
            try {
                if (field.isAnnotationPresent(Setting.class)) {
                    if (obj.has(field.getName())) {
                        new SettingField<>(settingsContainer, field, (Class<Object>)field.getType()).set(
                            deserializeField(obj.get(field.getName()), field.getType())
                        );
                    }
                } else if (Modifier.isFinal(field.getModifiers()) && field.getType().isAnnotationPresent(SettingsContainer.class)) {
                    if (obj.has(field.getName()))
                        deserialize(obj.get(field.getName()).getAsJsonObject(), field.get(settingsContainer));
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private static <T> T deserializeField(JsonElement element, Class<T> fieldClass) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
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
            T array = (T) Array.newInstance(arrElementClass, arr.size());
            for (int i = 0; i < arr.size(); ++i) {
                Array.set(array, i, deserializeField(arr.get(i), arrElementClass));
            }
            return array;
        } else {
            JsonObject obj = element.getAsJsonObject();
            return (T) deserialize(obj.get("value").getAsJsonObject(), Class.forName(obj.get("type").getAsString()));
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
