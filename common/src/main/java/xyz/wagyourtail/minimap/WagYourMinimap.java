package xyz.wagyourtail.minimap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.architectury.platform.Platform;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import xyz.wagyourtail.minimap.scanner.MapLevel;
import xyz.wagyourtail.minimap.scanner.updater.AbstractChunkUpdateStrategy;
import xyz.wagyourtail.minimap.scanner.updater.BlockUpdateStrategy;
import xyz.wagyourtail.minimap.scanner.updater.ChunkLoadStrategy;

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

public abstract class WagYourMinimap<T extends WagYourMinimapConfig> {
    public static final String MOD_ID = "wagyourminimap";
    public static final Path configFolder = Platform.getConfigFolder().resolve("WagYourMinimap");
    public static final Path configFile = configFolder.resolve("config.json");
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static final Logger LOGGER = LogManager.getLogger("WagYourMinimap");
    private static final Map<Class<? extends AbstractChunkUpdateStrategy>, AbstractChunkUpdateStrategy> chunkUpdateStrategies = new HashMap<>();
    public static WagYourMinimap<?> INSTANCE;
    public T config;
    public MapLevel currentLevel = null;


    public WagYourMinimap(Class<T> configClass) {
        initConfig(configClass);
        registerChunkUpdateStrategy(BlockUpdateStrategy.class);
        registerChunkUpdateStrategy(ChunkLoadStrategy.class);
    }

    public static void registerChunkUpdateStrategy(Class<? extends AbstractChunkUpdateStrategy> chunkUpdateStrategy) {
        chunkUpdateStrategies.computeIfAbsent(chunkUpdateStrategy, (cus) -> {
            try {
                return cus.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
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
                config = configClass.getConstructor().newInstance();
                saveConfig();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e2) {
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


    public String getServerName() {
        return ".";
    }

    public abstract String getLevelName(@Nullable Level level);

    public Level resolveServerLevel(Level level) {
        return level;
    }
}
