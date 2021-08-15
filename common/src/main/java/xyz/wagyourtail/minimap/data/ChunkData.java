package xyz.wagyourtail.minimap.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Range;
import xyz.wagyourtail.ResolveQueue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ChunkData implements AutoCloseable {
    private Map<String, Derivitive<?>> derrivitives = new HashMap<>();
    public long updateTime;

    public final int[] heightmap = new int[256];
    public final byte[] blocklight = new byte[256];
    public final int[] blockid = new int[256];
    public final int[] biomeid = new int[256];


    public final int[] oceanFloorHeightmap = new int[256];
    public final int[] oceanFloorBlockid = new int[256];
    public final int[] oceanFloorBiomeid = new int[256];

    private final List<ResourceLocation> resources = new ArrayList<>();

    public ChunkData() {
    }

    public synchronized int getOrRegisterResourceLocation(ResourceLocation id) {
        if (id == null) return 0;
        for (int j = 0; j < resources.size(); ++j) {
            if (id.equals(resources.get(j))) {
                return j + 1;
            }
        }
        resources.add(id);
        return resources.size();
    }

    public synchronized ResourceLocation getResourceLocation(@Range(from = 1, to = Integer.MAX_VALUE) int i) {
        return resources.get(i - 1);
    }

    public synchronized List<ResourceLocation> getResources() {
        return ImmutableList.copyOf(resources);
    }

    public synchronized <T> ResolveQueue<T> computeDerivitive(String key, Supplier<T> supplier) {
        //chunk is closed???
        if (derrivitives instanceof ImmutableMap) {
            Derivitive<T> der = (Derivitive<T>) derrivitives.get(key);
            if (der != null) {
                return der.contained;
            }
            return null;
        }
        Derivitive<T> der = (Derivitive<T>) derrivitives.computeIfAbsent(key, (k) -> new Derivitive<>(false, new ResolveQueue<>((n) -> supplier.get())));
        if (der.old) {
            der.old = false;
            der.contained.addTask(old -> supplier.get());
        }
        return der.contained;
    }

    Error firstClose = null;

    @Override
    public synchronized void close() {
        firstClose = new Error();
        derrivitives.forEach((k,v) -> {
            if (v.contained instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) v.contained).close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        derrivitives = ImmutableMap.copyOf(derrivitives);
    }

    public void copyDerivatives(ChunkData old) {
        this.derrivitives.putAll(old.getDerivativesAsOldAndClose());
    }

    private synchronized Map<String, Derivitive<?>> getDerivativesAsOldAndClose() {
        if (derrivitives == null) return ImmutableMap.of();
        Map<String, Derivitive<?>> derivitiveMap = ImmutableMap.copyOf(derrivitives);
        derivitiveMap.forEach((k,v) -> v.old = true);
        this.close();
        return derivitiveMap;
    }

    public synchronized void invalidateDerivitives() {
        if (derrivitives == null) return;
        derrivitives.values().forEach((v) -> v.old = true);
    }

    public static int blockPosToIndex(BlockPos pos) {
        int x = pos.getX() % 16;
        int z = pos.getZ() % 16;
        if (x < 0) x += 16;
        if (z < 0) z += 16;
        return (x << 4) + z;
    }

    public static class Derivitive<T>{
        public boolean old;
        public final ResolveQueue<T> contained;
        Derivitive(boolean old, ResolveQueue<T> contained) {
            this.old = old;
            this.contained = contained;
        }
    }

}
