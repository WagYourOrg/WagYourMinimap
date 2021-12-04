package xyz.wagyourtail.minimap.chunkdata.updater;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface BlockUpdateEvent {
    void onBlockUpdate(BlockPos pos, Level level);

}
