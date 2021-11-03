package xyz.wagyourtail.minimap.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.architectury.platform.Platform;
import net.minecraft.world.level.Level;
import xyz.wagyourtail.config.ConfigManager;
import xyz.wagyourtail.minimap.api.config.MinimapConfig;
import xyz.wagyourtail.minimap.map.MapServer;
import xyz.wagyourtail.minimap.map.chunkdata.cache.CacheManager;
import xyz.wagyourtail.minimap.map.chunkdata.updater.AbstractChunkUpdateStrategy;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class MinimapApi {
    public static final AtomicInteger saving = new AtomicInteger(0);
    public final CacheManager cacheManager = new CacheManager();
    protected static MinimapApi INSTANCE;

    protected final Map<Class<? extends AbstractChunkUpdateStrategy>, AbstractChunkUpdateStrategy> chunkUpdateStrategies = new HashMap<>();

    public final Path configFolder = Platform.getConfigFolder().resolve("WagYourMinimap");
    public final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private MapServer currentServer = null;
    protected ConfigManager config = new ConfigManager(configFolder.resolve("config.json"));

    protected MinimapApi() {
        INSTANCE = this;
        config.registerConfig("common", MinimapConfig.class);
    }

    public static MinimapApi getInstance() {
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

    public int getSaving() {
        return saving.get();
    }

    public ConfigManager getConfig() {
        return config;
    }

    public synchronized MapServer.MapLevel getMapLevel(Level current) {
        return getMapServer().getLevel(current);
    }

    public synchronized MapServer getMapServer() {
        if (currentServer == null || !getServerName().equals(currentServer.server_slug)) {
            currentServer = new MapServer(getServerName());
        }
        return currentServer;
    }

    public String getServerName() {
        return ".";
    }

    public void close() {
        cacheManager.close();
    }
}
