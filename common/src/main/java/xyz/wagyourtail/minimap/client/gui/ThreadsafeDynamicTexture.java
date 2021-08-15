package xyz.wagyourtail.minimap.client.gui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ThreadsafeDynamicTexture extends AbstractTexture {
    private static final Logger LOGGER = LogManager.getLogger();
    @Nullable
    private NativeImage pixels;

    public ThreadsafeDynamicTexture(@Nullable NativeImage nativeImage) {
        this.pixels = nativeImage;
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
                synchronized (this) {
                    if (this.pixels == null) return;
                    TextureUtil.prepareImage(this.getId(), this.pixels.getWidth(), this.pixels.getHeight());
                    this.upload();
                }
            });
        } else {
            synchronized (this) {
                if (this.pixels == null) return;
                TextureUtil.prepareImage(this.getId(), this.pixels.getWidth(), this.pixels.getHeight());
                this.upload();
            }
        }

    }

    public ThreadsafeDynamicTexture(int i, int j, boolean bl) {
        RenderSystem.assertThread(RenderSystem::isOnGameThreadOrInit);
        this.pixels = new NativeImage(i, j, bl);
        TextureUtil.prepareImage(this.getId(), this.pixels.getWidth(), this.pixels.getHeight());
    }

    @Override
    public void load(ResourceManager manager) {
    }

    public synchronized void upload() {
        if (this.pixels != null) {
            this.bind();
            this.pixels.upload(0, 0, 0, false);
        } else {
            LOGGER.warn("Trying to upload disposed texture");
        }

    }

    @Nullable
    public synchronized NativeImage getPixels() {
        return this.pixels;
    }

    public  synchronized void setPixels(@NotNull NativeImage nativeImage) {
        if (this.pixels != null) {
            this.pixels.close();
        }

        this.pixels = nativeImage;
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
                synchronized (this) {
                    if (this.pixels == null) return;
                    TextureUtil.prepareImage(this.getId(), this.pixels.getWidth(), this.pixels.getHeight());
                    this.upload();
                }
            });
        } else {
            if (this.pixels == null) return;
            TextureUtil.prepareImage(this.getId(), this.pixels.getWidth(), this.pixels.getHeight());
            this.upload();
        }
    }

    @Override
    public synchronized void close() {
        CompletableFuture.runAsync(() -> {
            synchronized (this) {
                if (this.pixels != null) {
                    this.pixels.close();
                    this.releaseId();
                    this.pixels = null;
                }
            }
        });
    }

}
