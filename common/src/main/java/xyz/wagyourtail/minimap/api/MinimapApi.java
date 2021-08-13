package xyz.wagyourtail.minimap.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.architectury.platform.Platform;
import net.minecraft.world.level.Level;
import xyz.wagyourtail.minimap.scanner.MapLevel;
import xyz.wagyourtail.minimap.scanner.updater.AbstractChunkUpdateStrategy;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public class MinimapApi {
    protected static MinimapApi INSTANCE;

    protected final Map<Class<? extends AbstractChunkUpdateStrategy>, AbstractChunkUpdateStrategy> chunkUpdateStrategies = new HashMap<>();

    public final Path configFolder = Platform.getConfigFolder().resolve("WagYourMinimap");
    public final Path configFile = configFolder.resolve("config.json");
    public final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    protected MapLevel currentLevel = null;
    protected WagYourMinimapConfig config = null;

    public static MinimapApi getInstance() {
        if (INSTANCE == null) INSTANCE = new MinimapApi();
        return INSTANCE;
    }

    public void registerChunkUpdateStrategy(Class<? extends AbstractChunkUpdateStrategy> chunkUpdateStrategy) {
        chunkUpdateStrategies.computeIfAbsent(chunkUpdateStrategy, (cus) -> {
            try {
                return cus.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
    }

    protected void getConfig(Class<? extends WagYourMinimapConfig> configClass) {
        if (config == null) {
            try {
                config = gson.fromJson(new FileReader(configFile.toFile()), configClass);
            } catch (Throwable e) {
                if (!(e instanceof FileNotFoundException)) {
                    if (configFile.toFile().exists()) {
                        final Path bak = configFolder.resolve("config.json.bak");
                        try {
                            Files.move(configFile, bak, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                try {
                    config = configClass.getConstructor().newInstance();
                    saveConfig();
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e2) {
                    throw new RuntimeException(e2);
                }
            }
        }
    }

    public synchronized WagYourMinimapConfig getConfig() {
        if (config == null) getConfig(WagYourMinimapConfig.class);
        return config;
    }

    public synchronized void saveConfig() {
        if (!configFolder.toFile().exists() && !configFolder.toFile().mkdirs()) throw new RuntimeException("Failed to create config folder!");
        try (FileWriter fw = new FileWriter(configFile.toFile())) {
            fw.write(gson.toJson(getConfig()));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public synchronized MapLevel getCurrentLevel() {
        return currentLevel;
    }

    public synchronized void setCurrentLevel(MapLevel level) {
        if (currentLevel != null) {
            currentLevel.close();
        }
        currentLevel = level;
    }

    public String getServerName() {
        return ".";
    }

    public String getLevelName(Level level) {
        return level.dimension().location().toString().replace(":", "_");
    }
}
