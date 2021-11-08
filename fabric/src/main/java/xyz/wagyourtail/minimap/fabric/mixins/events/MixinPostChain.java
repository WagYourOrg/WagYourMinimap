package xyz.wagyourtail.minimap.fabric.mixins.events;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.renderer.PostChain;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xyz.wagyourtail.minimap.fabric.IRenderTarget;

@Mixin(PostChain.class)
public class MixinPostChain {

    @Shadow @Final private RenderTarget screenTarget;

    @Inject(method = "addTempTarget", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onAddTempTarget(String name, int width, int height, CallbackInfo ci, RenderTarget target) {
        if (((IRenderTarget) this.screenTarget).isStencilEnabled()) {
            ((IRenderTarget) target).enableStencil();
        }
    }
}
