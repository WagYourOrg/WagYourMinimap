package xyz.wagyourtail.minimap.chunkdata.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.map.MapServer;
import xyz.wagyourtail.minimap.waypoint.Waypoint;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ZipCacher extends AbstractCacher {

    private final LoadingCache<Path, FileSystem> zipCache;

    public ZipCacher() {
        super(SaveOnLoad.IF_ABOVE, true);
        zipCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).removalListener(e -> {
            try {
                ((FileSystem) e.getValue()).close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).build(new CacheLoader<>() {
            @Override
            public FileSystem load(Path key) throws Exception {
                if (Files.notExists(key.getParent())) {
                    Files.createDirectories(key.getParent());
                }
                return FileSystems.newFileSystem(key, Map.of("create", true));
            }
        });
    }

    @Override
    public synchronized ChunkData loadChunk(ChunkLocation location) {
        FileSystem zipfs = getRegionZip(location);
        if (zipfs == null) {
            return null;
        }
        Path dataPath = zipfs.getPath(location.index() + ".data");
        Path blocksPath = zipfs.getPath(location.index() + ".blocks");
        Path biomesPath = zipfs.getPath(location.index() + ".biomes");
        if (Files.exists(dataPath) && Files.exists(blocksPath) && Files.exists(biomesPath)) {
            return loadFromDisk(location, dataPath, blocksPath, biomesPath);
        }
        return null;
    }

    public synchronized FileSystem getRegionZip(ChunkLocation location) {
        try {
            return zipCache.get(locationToPath(location));
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Path locationToPath(ChunkLocation location) {
        return MinimapApi.getInstance().configFolder.resolve(location.level().parent().server_slug)
            .resolve(location.level().level_slug())
            .resolve(location.getRegionSlug() + ".zip");
    }

    @Override
    public synchronized void saveChunk(ChunkLocation location, ChunkData data) {
        if (!data.changed) {
            return;
        }
        FileSystem zipfs = getRegionZip(location);
        if (zipfs == null) {
            throw new NullPointerException("Zip file system is null");
        }
        Path dataPath = zipfs.getPath(location.index() + ".data");
        Path blocksPath = zipfs.getPath(location.index() + ".blocks");
        Path biomesPath = zipfs.getPath(location.index() + ".biomes");
        writeToZip(dataPath, blocksPath, biomesPath, data);
    }

    private synchronized void writeToZip(Path dataPath, Path blocksPath, Path biomesPath, ChunkData chunk) {
        try {
            synchronized (chunk) {
                String blocks = chunk.serializeBlocks();
                Files.writeString(blocksPath, blocks);
                String biomes = chunk.serializeBiomes();
                Files.writeString(biomesPath, biomes);
                ByteBuffer data = chunk.serialize();
                Files.write(dataPath, data.array());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public synchronized void saveWaypoints(MapServer server, Stream<Waypoint> waypointList) {
        Path wpFile = serverPath(server).resolve("way.points");
        String points = waypointList.map(Waypoint::serialize).collect(Collectors.joining("\n"));
        try {
            Files.writeString(wpFile, points);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Path serverPath(MapServer server) {
        return MinimapApi.getInstance().configFolder.resolve(server.server_slug);
    }

    @Override
    public synchronized List<Waypoint> loadWaypoints(MapServer server) {
        Path wpFile = serverPath(server).resolve("way.points");
        if (Files.exists(wpFile)) {
            try {
                return Files.readAllLines(wpFile).stream().map(Waypoint::deserialize).collect(Collectors.toList());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    @Override
    public void close() {
        zipCache.invalidateAll();
        zipCache.cleanUp();
    }

    private ChunkData loadFromDisk(ChunkLocation location, Path dataPath, Path blocksPath, Path biomesPath) {
        ByteBuffer data;
        try (InputStream stream = Files.newInputStream(dataPath)) {
            data = ByteBuffer.wrap(stream.readAllBytes());
            data.rewind();
        } catch (IOException e) {
            throw new RuntimeException("Chunk data error", e);
        }
        String blocks;
        try (InputStream stream = Files.newInputStream(blocksPath)) {
            blocks = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new RuntimeException("Chunk resources error", ex);
        }
        try (InputStream stream = Files.newInputStream(biomesPath)) {
            return new ChunkData(location, data, blocks, new String(stream.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException ex) {
            throw new RuntimeException("Chunk resources error", ex);
        }
    }

}
