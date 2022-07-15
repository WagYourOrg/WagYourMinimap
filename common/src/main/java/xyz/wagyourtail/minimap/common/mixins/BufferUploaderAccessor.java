package xyz.wagyourtail.minimap.common.mixins;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.VertexBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BufferUploader.class)
public interface BufferUploaderAccessor {
    @Invoker("upload")
    public static VertexBuffer doUpload(BufferBuilder.RenderedBuffer renderedBuffer) {
        throw new AssertionError("Mixin not applied");
    }
}
