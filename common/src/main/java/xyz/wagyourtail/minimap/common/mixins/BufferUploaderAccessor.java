package xyz.wagyourtail.minimap.common.mixins;

import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.nio.ByteBuffer;

@Mixin(BufferUploader.class)
public interface BufferUploaderAccessor {
    @Invoker("_end")
    static void invoke_End(ByteBuffer buffer, VertexFormat.Mode mode, VertexFormat format, int vertexCount, VertexFormat.IndexType indexType, int indexCount, boolean sequentialIndex) {
    }

}
