package xyz.wagyourtail.minimap.forge.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.wagyourtail.minimap.client.world.InGameWaypointRenderer;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

    @Shadow public abstract Camera getMainCamera();

    @Inject(method = "renderLevel", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = "ldc=hand"))
    public void render(float partialTicks, long finishTimeNano, PoseStack poseStack, CallbackInfo ci) {
        InGameWaypointRenderer.RENDER_LAST.invoker().onRenderLast(poseStack, getMainCamera());
    }

}
