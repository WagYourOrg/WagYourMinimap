package xyz.wagyourtail.minimap.fabric.mixins.events;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import xyz.wagyourtail.minimap.fabric.IRenderTarget;

import java.nio.IntBuffer;

@Mixin(value = RenderTarget.class, priority = 999)
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
            target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V",
            remap = false
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
            GlStateManager._texImage2D(
                GL11.GL_TEXTURE_2D, 0,
                ARBFramebufferObject.GL_DEPTH24_STENCIL8,
                this.width,
                this.height,
                0,
                ARBFramebufferObject.GL_DEPTH_STENCIL,
                GL30.GL_UNSIGNED_INT_24_8,
                null
            );
        } else {
            GlStateManager._texImage2D(i, j, k, l, m, n, o, p, intBuffer);
        }
    }

    @Redirect(
        method = "createBuffers",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glFramebufferTexture2D(IIIII)V",
            remap = false
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
    public void onFramebufferTexture2D(int i, int j, int k, int l, int m) {
        if (stencilEnabled) {
            GlStateManager._glFramebufferTexture2D(
                i,
                GL30.GL_DEPTH_STENCIL_ATTACHMENT,
                GL11.GL_TEXTURE_2D,
                this.depthBufferId,
                0
            );
        } else {
            GlStateManager._glFramebufferTexture2D(i, j, k, l, m);
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
