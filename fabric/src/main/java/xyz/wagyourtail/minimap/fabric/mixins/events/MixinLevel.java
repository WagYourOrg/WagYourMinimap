package xyz.wagyourtail.minimap.fabric.mixins.events;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.wagyourtail.minimap.chunkdata.updater.AbstractChunkDataUpdater;

@Mixin(Level.class)
public class MixinLevel {

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
        at = @At("RETURN"))
    public void onSetBlock(BlockPos pos, BlockState state, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> cir) {
        if (((Object) this) instanceof ClientLevel) {
            AbstractChunkDataUpdater.BLOCK_UPDATE.invoker().onBlockUpdate(pos, (Level) (Object) this);
        }
    }

}
