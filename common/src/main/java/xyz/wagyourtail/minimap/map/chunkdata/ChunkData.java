package xyz.wagyourtail.minimap.map.chunkdata;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import xyz.wagyourtail.ResolveQueue;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.map.MapServer;
import xyz.wagyourtail.minimap.map.chunkdata.cache.AbstractCacher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ChunkData implements AutoCloseable {
    private static final ResourceLocation air = new ResourceLocation("minecraft", "air");
    private final List<ResourceLocation> resources = new ArrayList<>();
    public final ChunkLocation location;
    public final int[] heightmap = new int[256];
    public final byte[] blocklight = new byte[256];
    public final int[] blockid = new int[256];
    public final int[] biomeid = new int[256];
    public final int[] oceanFloorHeightmap = new int[256];
    public final int[] oceanFloorBlockid = new int[256];
    private Map<String, Derivitive<?>> derrivitives = new HashMap<>();
    Error firstClose = null;
    public long updateTime;
    public boolean changed = false;

    public ChunkData(ChunkLocation location) {
        this.location = location;
    }

    public static int blockPosToIndex(int posX, int posZ) {
        int x = posX % 16;
        int z = posZ % 16;
        if (x < 0) x += 16;
        if (z < 0) z += 16;
        return (x << 4) + z;
    }

    public static int blockPosToIndex(BlockPos pos) {
        int x = pos.getX() % 16;
        int z = pos.getZ() % 16;
        if (x < 0) x += 16;
        if (z < 0) z += 16;
        return (x << 4) + z;
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

    public synchronized ResourceLocation getResourceLocation(int i) {
        if (i < 1) return air;
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
            der.contained.addTask(old -> supplier.get(), 2);
        }
        return der.contained;
    }

    @Override
    public synchronized void close() {
        if (firstClose != null) return;
        firstClose = new Error();
        if (derrivitives instanceof ImmutableMap) return;
        derrivitives.forEach((k, v) -> {
            if (v.contained instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) v.contained).close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        derrivitives = ImmutableMap.of();
    }

    public synchronized void combineWithNewData(ChunkData newData) {
        if (newData.updateTime > this.updateTime) {
            this.updateTime = newData.updateTime;
            this.changed = true;
            boolean changed = false;
            for (int i = 0; i < 256; ++i) {
                int newHeight = newData.heightmap[i];
                changed = changed || newHeight != this.heightmap[i];
                this.heightmap[i] = newHeight;

                byte newLight = newData.blocklight[i];
                changed = changed || newLight != this.blocklight[i];
                this.blocklight[i] = newLight;

                int newBlockid = newData.blockid[i];
                changed = changed || newBlockid != this.blockid[i];
                this.blockid[i] = newBlockid;

                int newBiomeId = newData.biomeid[i];
                changed = changed || newBiomeId != this.biomeid[i];
                this.biomeid[i] = newBiomeId;

                int newOceanHeight = newData.oceanFloorHeightmap[i];
                changed = changed || newOceanHeight != this.oceanFloorHeightmap[i];
                this.oceanFloorHeightmap[i] = newOceanHeight;

                int newOceanBlock = newData.oceanFloorBlockid[i];
                changed = changed || newOceanBlock != this.oceanFloorBlockid[i];
                this.oceanFloorBlockid[i] = newOceanBlock;
            }
            this.resources.clear();
            this.resources.addAll(newData.resources);
            if (changed) markDirty();
        }
    }

    public synchronized void markDirty() {
        if (derrivitives == null) return;
        derrivitives.values().forEach((v) -> v.old = true);
        changed = true;
        MapServer.addToSaveQueue(() -> {
            synchronized (this) {
                for (AbstractCacher cacher : MinimapApi.getInstance().getCachers()) {
                    cacher.saveChunk(location, this);
                }
            }
        });
    }

    public static class Derivitive<T> {
        public final ResolveQueue<T> contained;
        public boolean old;

        Derivitive(boolean old, ResolveQueue<T> contained) {
            this.old = old;
            this.contained = contained;
        }

    }

}
