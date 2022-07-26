package xyz.wagyourtail.minimap.common.mixins;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.ChunkSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.wagyourtail.minimap.chunkdata.updater.AbstractChunkDataUpdater;

@Mixin(ClientChunkCache.class)
public abstract class MixinClientChunkCache extends ChunkSource {

    @Inject(method = {"onLightUpdate"}, at = @At("HEAD"))
    public void onSetLevel(LightLayer layer, SectionPos pos, CallbackInfo ci) {
        if (layer == LightLayer.BLOCK)
            AbstractChunkDataUpdater.LIGHT_LEVEL.invoker().onLightLevel(this, pos);
    }
}
