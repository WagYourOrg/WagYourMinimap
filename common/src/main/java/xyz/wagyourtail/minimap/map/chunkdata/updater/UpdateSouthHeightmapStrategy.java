package xyz.wagyourtail.minimap.map.chunkdata.updater;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.map.chunkdata.parts.SouthHeightmap;

public class UpdateSouthHeightmapStrategy extends AbstractChunkUpdateStrategy<SouthHeightmap> {
    public static final Event<Update> UPDATE_EVENT = EventFactory.createLoop();

    @Override
    protected void registerEventListener() {
        UPDATE_EVENT.register((location, heightmap) -> updateChunk(location,
           (cl, parent, oldData) -> updateNorthHeightmap(parent, oldData, heightmap)
        ));
    }

    public SouthHeightmap updateNorthHeightmap(ChunkData parent, SouthHeightmap oldData, int[] northHeightmap) {
        SouthHeightmap data = new SouthHeightmap(parent);

        for (int i = 0; i < 16; ++i) {
            data.heightmap[i] = northHeightmap[16 * i];
        }

        if (oldData != null) {
            oldData.mergeFrom(data);
            return oldData;
        }
        data.parent.markDirty();
        return data;
    }

    @Override
    public Class<SouthHeightmap> getType() {
        return SouthHeightmap.class;
    }

    public interface Update {
        void onUpdate(ChunkLocation location, int[] heightmap);

    }
}
