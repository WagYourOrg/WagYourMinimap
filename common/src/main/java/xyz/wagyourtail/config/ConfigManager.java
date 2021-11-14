package xyz.wagyourtail.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ConfigManager {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<Class<?>, String> configRegistry = new LinkedHashMap<>();
    private final Path configPath;
    private final Map<Class<?>, Object> config = new HashMap<>();
    private JsonObject rawConfig;
    private boolean dirty = false;

    public ConfigManager(Path configPath) {
        this.configPath = configPath;
    }

    public void registerConfig(String key, Class<?> config) {
        synchronized (configRegistry) {
            configRegistry.put(config, key);
        }
        get(config);
    }

    /**
     * @param configClass the config type to query
     * @param <T>
     *
     * @return instance of queried config.
     */
    public synchronized <T> T get(Class<T> configClass) {
        T cfg = (T) config.computeIfAbsent(configClass, this::loadConfig);
        if (dirty) {
            saveConfig();
        }
        return cfg;
    }

    /**
     * @param configClass
     * @param <T>
     *
     * @return
     */
    private <T> T loadConfig(Class<T> configClass) {
        synchronized (configRegistry) {
            if (!configRegistry.containsKey(configClass)) {
                throw new NullPointerException("Unknown Config!");
            }
            if (rawConfig == null) {
                reloadConfigFromDisk();
            }
            if (!rawConfig.has(configRegistry.get(configClass))) {
                try {
                    dirty = true;
                    return configClass.getConstructor().newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                return SettingContainerSerializer.deserialze(rawConfig.get(configRegistry.get(configClass))
                    .getAsJsonObject(), configClass);
            } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     *
     */
    public void reloadConfigFromDisk() {
        synchronized (configPath) {
            if (!Files.exists(configPath)) {
                rawConfig = new JsonObject();
                return;
            }
            try {
                rawConfig = new JsonParser().parse(Files.newBufferedReader(configPath)).getAsJsonObject();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void saveConfig() {
        synchronized (configPath) {
            configPath.getParent().toFile().mkdirs();
            synchronized (configRegistry) {
                try {
                    for (Map.Entry<Class<?>, Object> config : config.entrySet()) {
                        rawConfig.add(
                            configRegistry.get(config.getKey()),
                            SettingContainerSerializer.serialize(config.getValue())
                        );
                    }
                    Files.writeString(configPath, gson.toJson(rawConfig));
                } catch (IOException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                dirty = false;
            }
        }
    }

    public Set<Class<?>> getRegisteredConfigs() {
        synchronized (configRegistry) {
            return configRegistry.keySet();
        }
    }

}
