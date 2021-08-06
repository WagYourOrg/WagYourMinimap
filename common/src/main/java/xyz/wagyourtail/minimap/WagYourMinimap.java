package xyz.wagyourtail.minimap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.architectury.platform.Platform;
import xyz.wagyourtail.minimap.scanner.MapLevel;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public abstract class WagYourMinimap<T extends WagYourMinimapConfig> {
    public static final String MOD_ID = "wagyourminimap";
    public static final Path configFolder = Platform.getConfigFolder().resolve("WagYourMinimap");
    public static final Path configFile = configFolder.resolve("config.json");
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static WagYourMinimap<?> INSTANCE;
    public T config;
    public MapLevel currentLevel = null;

    public WagYourMinimap(Class<T> configClass) {
        initConfig(configClass);
    }

    private void initConfig(Class<T> configClass) {
        try {
            config = gson.fromJson(new FileReader(configFile.toFile()), configClass);
        } catch (Throwable e) {
            if (!(e instanceof FileNotFoundException)) {
                if (configFile.toFile().exists()) {
                    final Path bak = configFolder.resolve("config.json.bak");
                    try {
                        Files.move(configFile, bak, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException ignored) {}
                }
            }
            try {
                config = configClass.newInstance();
                saveConfig();
            } catch (InstantiationException | IllegalAccessException e2) {
                throw new RuntimeException(e2);
            }
        }
    }

    public void saveConfig() {
        if (!configFolder.toFile().exists() && !configFolder.toFile().mkdirs()) throw new RuntimeException("Failed to create config folder!");
        try (FileWriter fw = new FileWriter(configFile.toFile())) {
            fw.write(gson.toJson(config));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
