package xyz.wagyourtail.minimap.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.architectury.platform.Platform;
import net.minecraft.world.level.Level;
import xyz.wagyourtail.minimap.api.config.MinimapConfig;
import xyz.wagyourtail.minimap.map.MapLevel;
import xyz.wagyourtail.minimap.map.MapServer;
import xyz.wagyourtail.minimap.map.chunkdata.cache.AbstractCacher;
import xyz.wagyourtail.minimap.map.chunkdata.updater.AbstractChunkUpdateStrategy;
import xyz.wagyourtail.config.ConfigManager;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class MinimapApi {
    public static final AtomicInteger saving = new AtomicInteger(0);
    protected static MinimapApi INSTANCE;
    private final List<AbstractCacher> cachers = new ArrayList<>();

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

    public synchronized void addCacherAfter(Class<? extends AbstractCacher> cacher, Class<? extends AbstractCacher> after) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        int i = 0;
        for (AbstractCacher abstractCacher : cachers) {
            ++i;
            if (abstractCacher.getClass().equals(after)) {
                break;
            }
        }
        cachers.add(i, cacher.getConstructor().newInstance());
    }

    public synchronized void addCacherBefore(Class<? extends AbstractCacher> cacher, Class<? extends AbstractCacher> before) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        int i = 0;
        for (AbstractCacher abstractCacher : cachers) {
            if (abstractCacher.getClass().equals(before)) {
                break;
            }
            ++i;
        }
        cachers.add(i, cacher.getConstructor().newInstance());
    }

    public void removeCacher(Class<? extends AbstractCacher> cacher) {
        cachers.remove(cacher);
    }

    public List<AbstractCacher> getCachers() {
        return ImmutableList.copyOf(cachers);
    }

    public int getSaving() {
        return saving.get();
    }

    public ConfigManager getConfig() {
        return config;
    }

    public synchronized MapLevel getMapLevel(Level current) {
        return getMapServer().getLevel(current);
    }

    public synchronized MapServer getMapServer() {
        if (currentServer == null || !getServerName().equals(currentServer.server_slug)) {
            if (currentServer != null) {
                currentServer.close();
            }
            currentServer = new MapServer(getServerName());
        }
        return currentServer;
    }

    public String getServerName() {
        return ".";
    }

    public void close() {
        if (currentServer != null) {
            currentServer.close();
        }
        for (AbstractCacher cacher : cachers) {
            cacher.close();
        }
    }

}
