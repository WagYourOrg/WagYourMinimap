package xyz.wagyourtail.minimap.fabric.mixins.events;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.wagyourtail.minimap.fabric.IRenderTarget;

import java.nio.IntBuffer;

@Mixin(RenderTarget.class)
public abstract class MixinRenderTarget implements IRenderTarget {

    @Shadow
    protected int depthBufferId;
    @Shadow
    public int width;
    @Shadow
    public int height;
    @Shadow
    public int viewWidth;
    @Shadow
    public int viewHeight;
    @Unique
    public boolean stencilEnabled = false;

    @Redirect(method = "createBuffers",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V"
        ),
        slice = @Slice(
            from = @At(value = "FIELD",
                target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;useDepth:Z",
                ordinal = 0
            ),
            to = @At(
                value = "INVOKE",
                target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;setFilterMode(I)V"
            )
        )
    )
    public void onTexImage2D(int i, int j, int k, int l, int m, int n, int o, int p, IntBuffer intBuffer) {
        if (stencilEnabled) {
            GlStateManager._texImage2D(3553, 0, 36013, this.width, this.height, 0, 34041, 36269, null);
        } else {
            GlStateManager._texImage2D(i, j, k, l, m, n, o, p, intBuffer);
        }
    }

    @Inject(
        method = "createBuffers",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glFramebufferTexture2D(IIIII)V",
            shift = At.Shift.AFTER
        ),
        slice = @Slice(
            from = @At(
                value = "FIELD",
                target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;useDepth:Z",
                ordinal = 1
            ),
            to = @At(
                value = "INVOKE",
                target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;checkStatus()V"
            )
        )
    )
    public void onFramebufferTexture2D(int width, int height, boolean clearError, CallbackInfo ci) {
        if (stencilEnabled) {
            GlStateManager._glFramebufferTexture2D(36160, 36128, 3553, this.depthBufferId, 0);
        }
    }

    @Unique
    @Override
    public boolean isStencilEnabled() {
        return stencilEnabled;
    }


    @Override
    public void enableStencil() {
        if (!this.stencilEnabled) {
            this.stencilEnabled = true;
            this.resize(this.viewWidth, this.viewHeight, Minecraft.ON_OSX);
        }
    }

    @Shadow
    public abstract void resize(int width, int height, boolean clearError);

}
