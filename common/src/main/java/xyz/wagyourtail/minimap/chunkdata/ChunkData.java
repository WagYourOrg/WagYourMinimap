package xyz.wagyourtail.minimap.chunkdata;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.chunkdata.parts.DataPart;
import xyz.wagyourtail.minimap.map.MapServer;

import java.lang.reflect.InvocationTargetException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ChunkData {
    private final Map<Class<? extends DataPart<?>>, DataPart<?>> data = new HashMap<>();
    public final ChunkLocation location;
    private static final ResourceLocation air = new ResourceLocation("minecraft", "air");
    private final List<ResourceLocation> resources = new ArrayList<>();
    private Map<String, Derivative<?>> derivatives = new HashMap<>();
    public long updateTime;
    public boolean changed = false;

    /**
     * empty data
     */
    public ChunkData(ChunkLocation location) {
        this.location = location;
    }

    /**
     * Data Specificaion:
     * long: updateTime
     * [
     * int: string length
     * string: classname
     * int: data length
     * byte[]: data
     * ]
     * @param buffer bytes to deserialize
     */
    public ChunkData(ChunkLocation location, ByteBuffer buffer, String resources) {
        this.location = location;
        try {
            updateTime = buffer.getLong();
            while (buffer.hasRemaining()) {
                int strSize = buffer.getInt();
                byte[] strBytes = new byte[strSize];
                buffer.get(strBytes);
                String className = new String(strBytes);
                Class<? extends DataPart<?>> clazz;
                DataPart<?> dp;
                try {
                    Map<String, String> classRemapper = Map.of("xyz.wagyourtail.minimap.map.chunkdata.parts.SurfaceDataPart", "xyz.wagyourtail.minimap.chunkdata.parts.SurfaceDataPart");
                    clazz = (Class<? extends DataPart<?>>) Class.forName(classRemapper.getOrDefault(className, className));
                    data.put(
                        clazz,
                        dp = clazz.getConstructor(ChunkData.class).newInstance(this)
                    );
                } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                    System.out.println("Failed to deserialize data part: " + className);
                    // discard unknown data
                    buffer.get(new byte[buffer.getInt()]);
                    continue;
                }
                int size = buffer.getInt();
                if (dp.getBytes() != size) {
                    try {
                        throw new AssertionError("Invalid data size for " + this.getClass().getCanonicalName());
                    } catch (AssertionError e) {
                        e.printStackTrace();
                        buffer.get(new byte[size]);
                    }
                } else {
                    dp.deserialize(buffer);
                }
            }
        } catch (BufferUnderflowException e) {
            e.printStackTrace();
            System.err.println("Buffer underflow, data is probably corrupted, " + location.getChunkX() + "," + location.getChunkZ() + "(" + location.getRegionSlug() + ":" + location.index() + ")");
        }
        for (String s : resources.split("\n")) {
            this.resources.add(new ResourceLocation(s));
        }
    }

    public int getOrRegisterResourceLocation(ResourceLocation id) {
        if (id == null || id.equals(air)) return 0;
        for (int j = 0; j < resources.size(); ++j) {
            if (id.equals(resources.get(j))) {
                return j + 1;
            }
        }
        resources.add(id);
        return resources.size();
    }

    public ResourceLocation getResourceLocation(int i) {
        if (i < 1 || i > resources.size()) return air;
        return resources.get(i - 1);
    }

    public int highestResourceValue() {
        return resources.size();
    }

    public String serializeResources() {
        StringBuilder sb = new StringBuilder();
        for (ResourceLocation rl : resources) {
            sb.append(rl.toString()).append("\n");
        }
        return sb.toString();
    }

    public <T extends DataPart<T>> Optional<T> getData(Class<T> clazz) {
        return Optional.ofNullable((T) data.get(clazz));
    }

    public synchronized <T extends DataPart<?>> T computeData(Class<T> clazz, Function<T, T> computeFunc) {
        try {
            return (T) data.compute(clazz, (k, v) -> computeFunc.apply((T) v));
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return (T) data.get(clazz);
    }

    @SuppressWarnings("unchecked")
    public <T> T computeDerivative(String key, Supplier<T> supplier) {
        return (T) derivatives.compute(key, (k, v) -> {
            if (v == null) {
                v = new Derivative<>(false, supplier.get());
            }
            if (v.old) {
                v.old = false;
                ((Derivative<T>) v).setContained(supplier.get());
            }
            return v;
        }).getContained();
    }

    public void invalidateDerivitives() {
        if (derivatives == null) return;
        for (Derivative<?> der : derivatives.values()) {
            der.old = true;
        }
    }

    public void markDirty() {
        changed = true;
        invalidateDerivitives();
        MapServer.addToSaveQueue(this::save);
    }

    public void save() {
        refactorResourceLocations();
        MinimapApi.getInstance().cacheManager.saveChunk(location, this);
        changed = false;
    }

    public void closeDerivatives() {
        if (derivatives == null) return;
        for (Derivative<?> der : derivatives.values()) {
            der.old = true;
            der.setContained(null);
        }
    }

    public synchronized void refactorResourceLocations() {
        Set<Integer> used = new HashSet<>(resources.size());
        for (DataPart<?> value : data.values()) {
            value.usedResourceLocations(used);
        }
        List<ResourceLocation> oldResources = ImmutableList.copyOf(resources);
        Map<Integer, Integer> transform = new Int2IntOpenHashMap();
        try {
            for (int i = resources.size(); i > 0; --i) {
                if (!used.contains(i)) {
                    resources.remove(i - 1);
                }
            }
            transform.put(0, 0);
            for (int i = 0, j = 0; i < resources.size(); ++i) {
                ResourceLocation newR = resources.get(i);
                while (!newR.equals(oldResources.get(j))) ++j;
                transform.put(j + 1, i + 1);
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            // in case of error, undo the changes
            resources.clear();
            resources.addAll(oldResources);
            return;
        }
        for (DataPart<?> value : data.values()) {
            try {
                value.remapResourceLocations(transform);
            } catch (NullPointerException e) {
                throw new RuntimeException(
                    "Error while remapping resource locations [" +
                    oldResources.stream().map(ResourceLocation::toString).collect(Collectors.joining(", ")) +
                    "] -> [" +
                    resources.stream().map(ResourceLocation::toString).collect(Collectors.joining(", ")) +
                   "]", e);
            }
        }
    }

    public ByteBuffer serialize() {
        int size = Long.BYTES;
        for (DataPart<?> dp : data.values()) {
            size += dp.getClass().getCanonicalName().getBytes(StandardCharsets.UTF_8).length;
            size += dp.getBytes();
            size += Integer.BYTES * 2;
        }
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.putLong(updateTime);
        for (DataPart<?> dp : data.values()) {
            byte[] strBytes = dp.getClass().getCanonicalName().getBytes(StandardCharsets.UTF_8);
            buffer.putInt(strBytes.length);
            buffer.put(strBytes);
            buffer.putInt(dp.getBytes());
            dp.serialize(buffer);
        }
        return buffer;
    }

    public ChunkLocation north() {
        return ChunkLocation.locationForChunkPos(location.level(), location.getChunkX(), location.getChunkZ() - 1);
    }

    public ChunkLocation south()  {
        return ChunkLocation.locationForChunkPos(location.level(), location.getChunkX(), location.getChunkZ() + 1);
    }

    public ChunkLocation west() {
        return ChunkLocation.locationForChunkPos(location.level(), location.getChunkX() - 1, location.getChunkZ());
    }

    public ChunkLocation east() {
        return ChunkLocation.locationForChunkPos(location.level(), location.getChunkX() + 1, location.getChunkZ());
    }
}
