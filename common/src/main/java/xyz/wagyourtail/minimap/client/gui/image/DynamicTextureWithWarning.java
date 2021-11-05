package xyz.wagyourtail.minimap.client.gui.image;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;

public class DynamicTextureWithWarning extends DynamicTexture {
    boolean closed = false;
    public DynamicTextureWithWarning(NativeImage nativeImage) {
        super(nativeImage);
    }

    public DynamicTextureWithWarning(int i, int j, boolean bl) {
        super(i, j, bl);
    }

    @Override
    public void close() {
        closed = true;
        super.close();
    }


    //TODO: make this not happen xd
    @Override
    protected void finalize() throws Throwable {
        if (!closed) {
            System.err.println("WARNING: DynamicTexture was not closed! memory leak!");
            close();
        }
    }

}
