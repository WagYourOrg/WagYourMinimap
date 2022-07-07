package xyz.wagyourtail.minimap.chunkdata;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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

@SuppressWarnings("unchecked")
public class ChunkData {
    private static final ResourceLocation plains = new ResourceLocation("minecraft", "plains");
    private static final JsonParser parser = new JsonParser();
    private static final Gson gson = new Gson();

    private final Map<Class<? extends DataPart<?>>, DataPart<?>> data = new HashMap<>();
    private final List<BlockState> blocks = new ArrayList<>();
    private final List<ResourceLocation> biomes = new ArrayList<>();
    private final Map<String, Derivative<?>> derivatives = new HashMap<>();
    public final ChunkLocation location;
    public long updateTime;
    public boolean changed = false;

    /**
     * empty data
     */
    public ChunkData(ChunkLocation location) {
        this.location = location;
    }

    /**
     * Data Specification:
     * long: updateTime
     * [
     * int: string length
     * string: classname
     * int: data length
     * byte[]: data
     * ]
     *
     * @param buffer bytes to deserialize
     */
    public ChunkData(ChunkLocation location, ByteBuffer buffer, String blocks, String biomes) {
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
                    Map<String, String> classRemapper = Map.of(
                        "xyz.wagyourtail.minimap.map.chunkdata.parts.SurfaceDataPart",
                        "xyz.wagyourtail.minimap.chunkdata.parts.SurfaceDataPart"
                    );
                    clazz = (Class<? extends DataPart<?>>) Class.forName(classRemapper.getOrDefault(
                        className,
                        className
                    ));
                    data.put(clazz, dp = clazz.getConstructor(ChunkData.class).newInstance(this));
                } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                         InstantiationException | IllegalAccessException e) {
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
            System.err.println(
                "Buffer underflow, data is probably corrupted, " + location.getChunkX() + "," + location.getChunkZ() +
                    "(" + location.getRegionSlug() + ":" + location.index() + ")");
        }
        for (String s : blocks.split("\n")) {
            try {
                this.blocks.add(BlockState.CODEC.decode(JsonOps.INSTANCE, parser.parse(s))
                    .result()
                    .orElseGet(() -> new Pair<>(Blocks.AIR.defaultBlockState(), null))
                    .getFirst());
            } catch (JsonParseException e) {
                System.out.println("Failed to deserialize block: " + s);
                e.printStackTrace();
            }
        }
        for (String s : biomes.split("\n")) {
            this.biomes.add(new ResourceLocation(s));
        }
    }

    public int getOrRegisterBlockState(BlockState state) {
        if (state == null) {
            return 0;
        }
        for (int j = 0; j < blocks.size(); ++j) {
            if (state.equals(blocks.get(j))) {
                return j + 1;
            }
        }
        blocks.add(state);
        return blocks.size();
    }

    public BlockState getBlockState(int i) {
        if (i < 1 || i > blocks.size()) {
            return Blocks.AIR.defaultBlockState();
        }
        return blocks.get(i - 1);
    }

    public int getOrRegisterBiome(ResourceLocation biome) {
        for (int i = 0; i < biomes.size(); ++i) {
            if (biomes.get(i).equals(biome)) {
                return i + 1;
            }
        }
        biomes.add(biome);
        return biomes.size();
    }

    public ResourceLocation getBiome(int i) {
        if (i < 1 || i > biomes.size()) {
            return plains;
        }
        return biomes.get(i - 1);
    }

    public int highestBlockValue() {
        return blocks.size();
    }

    public String serializeBlocks() {
        StringBuilder sb = new StringBuilder();
        for (BlockState state : blocks) {
            sb.append(gson.toJson(BlockState.CODEC.encodeStart(JsonOps.INSTANCE, state).result().orElseThrow())).append(
                "\n");
        }
        return sb.toString();
    }

    public String serializeBiomes() {
        StringBuilder sb = new StringBuilder();
        for (ResourceLocation biome : biomes) {
            sb.append(biome.toString()).append("\n");
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

    public <T> T computeDerivative(String key, Supplier<T> supplier) {
        return (T) Optional.ofNullable(derivatives.compute(key, (k, v) -> {
            if (v == null) {
                T t = supplier.get();
                if (t == null) {
                    return null;
                }
                v = new Derivative<>(false, t);
            }
            if (v.old) {
                T t = supplier.get();
                if (t == null) {
                    return v;
                }
                v.old = false;
                ((Derivative<T>) v).setContained(t);
            }
            return v;
        })).map(Derivative::getContained).orElse(null);
    }

    public void markDirty() {
        changed = true;
        MapServer.addToSaveQueue(this::save);
    }

    public void save() {
        refactorBlockStates();
        refactorBiomes();
        MinimapApi.getInstance().cacheManager.saveChunk(location, this);
        changed = false;
    }

    public synchronized void refactorBlockStates() {
        Set<Integer> used = new HashSet<>(blocks.size());
        for (DataPart<?> value : data.values()) {
            value.usedBlockStates(used);
        }
        List<BlockState> oldBlocks = ImmutableList.copyOf(blocks);
        Map<Integer, Integer> transform = new Int2IntOpenHashMap();
        try {
            for (int i = blocks.size(); i > 0; --i) {
                if (!used.contains(i)) {
                    blocks.remove(i - 1);
                }
            }
            transform.put(0, 0);
            for (int i = 0, j = 0; i < blocks.size(); ++i) {
                BlockState newR = blocks.get(i);
                while (!newR.equals(oldBlocks.get(j))) {
                    ++j;
                }
                transform.put(j + 1, i + 1);
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            // in case of error, undo the changes
            blocks.clear();
            blocks.addAll(oldBlocks);
            return;
        }
        for (DataPart<?> value : data.values()) {
            try {
                value.remapBlockStates(transform);
            } catch (NullPointerException e) {
                throw new RuntimeException("Error while remapping block states [" +
                    oldBlocks.stream().map(BlockState::toString).collect(Collectors.joining(", ")) + "] -> [" +
                    blocks.stream().map(BlockState::toString).collect(Collectors.joining(", ")) + "]", e);
            }
        }
    }

    public synchronized void refactorBiomes() {
        Set<Integer> used = new HashSet<>(biomes.size());
        for (DataPart<?> value : data.values()) {
            value.usedBiomes(used);
        }
        List<ResourceLocation> oldBiomes = ImmutableList.copyOf(biomes);
        Map<Integer, Integer> transform = new Int2IntOpenHashMap();
        try {
            for (int i = biomes.size(); i > 0; --i) {
                if (!used.contains(i)) {
                    biomes.remove(i - 1);
                }
            }
            transform.put(0, 0);
            for (int i = 0, j = 0; i < biomes.size(); ++i) {
                ResourceLocation newR = biomes.get(i);
                while (!newR.equals(oldBiomes.get(j))) {
                    ++j;
                }
                transform.put(j + 1, i + 1);
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            // in case of error, undo the changes
            biomes.clear();
            biomes.addAll(oldBiomes);
        }
        for (DataPart<?> value : data.values()) {
            try {
                value.remapBiomes(transform);
            } catch (NullPointerException e) {
                throw new RuntimeException("Error while remapping biomes [" +
                    oldBiomes.stream().map(ResourceLocation::toString).collect(Collectors.joining(", ")) + "] -> [" +
                    biomes.stream().map(ResourceLocation::toString).collect(Collectors.joining(", ")) + "]", e);
            }
        }
    }

    /**
     * invalidates derivitives whose keys start with the given prefixes
     *
     * @param prefixes
     */
    public void invalidateDerivitives(Set<String> prefixes) {
        for (Map.Entry<String, Derivative<?>> der : derivatives.entrySet()) {
            if (prefixes.stream().anyMatch(e -> e.contains(der.getKey()))) {
                der.getValue().old = true;
            }
        }
    }

    public void closeDerivatives() {
        for (Derivative<?> der : derivatives.values()) {
            der.old = true;
            der.setContained(null);
        }
    }

    public ByteBuffer serialize() {
        int size = Long.BYTES;
        for (DataPart<?> dp : data.values()) {
            if (dp.getBytes() > 0) {
                size += dp.getClass().getCanonicalName().getBytes(StandardCharsets.UTF_8).length;
                size += dp.getBytes();
                size += Integer.BYTES * 2;
            }
        }
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.putLong(updateTime);
        for (DataPart<?> dp : data.values()) {
            if (dp.getBytes() > 0) {
                byte[] strBytes = dp.getClass().getCanonicalName().getBytes(StandardCharsets.UTF_8);
                buffer.putInt(strBytes.length);
                buffer.put(strBytes);
                buffer.putInt(dp.getBytes());
                dp.serialize(buffer);
            }
        }
        return buffer;
    }

    public ChunkLocation north() {
        return ChunkLocation.locationForChunkPos(location.level(), location.getChunkX(), location.getChunkZ() - 1);
    }

    public ChunkLocation south() {
        return ChunkLocation.locationForChunkPos(location.level(), location.getChunkX(), location.getChunkZ() + 1);
    }

    public ChunkLocation west() {
        return ChunkLocation.locationForChunkPos(location.level(), location.getChunkX() - 1, location.getChunkZ());
    }

    public ChunkLocation east() {
        return ChunkLocation.locationForChunkPos(location.level(), location.getChunkX() + 1, location.getChunkZ());
    }

}
