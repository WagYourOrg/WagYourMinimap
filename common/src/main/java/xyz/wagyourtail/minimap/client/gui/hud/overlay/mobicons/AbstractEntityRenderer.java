package xyz.wagyourtail.minimap.client.gui.hud.overlay.mobicons;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.LivingEntity;

public abstract class AbstractEntityRenderer<T extends LivingEntity> {
    protected static final Minecraft minecraft = Minecraft.getInstance();

    public static <T extends LivingEntity> Part<T> part(float x, float y, float w, float h, float u, float v, float uw, float vh) {
        return new Part<>(x, y, w, h, u, v, uw, vh);
    }

    public abstract boolean canUseFor(LivingEntity entity);

    public abstract void render(PoseStack stack, LivingEntity entity, float maxSize, double yDiff);

    public static class Parts<T extends LivingEntity> {
        private final int texWidth;
        private final int texHeight;
        private final float scale;
        private final Part<T>[] parts;

        @SafeVarargs
        public Parts(int texWidth, int texHeight, float scale, Part<T>... parts) {
            this.texWidth = texWidth;
            this.texHeight = texHeight;
            this.scale = scale;
            this.parts = parts;
        }

        public void render(PoseStack stack, T entity, float maxSize, double yDiff) {
            if (Math.abs(yDiff) >= 1) {
                return; // don't render if too far away on y axis
            }
            float minX = Float.POSITIVE_INFINITY;
            float maxX = Float.NEGATIVE_INFINITY;
            float minY = Float.POSITIVE_INFINITY;
            float maxY = Float.NEGATIVE_INFINITY;
            for (Part<T> part : parts) {
                minX = Math.min(minX, part.x);
                maxX = Math.max(maxX, part.x + part.w);
                minY = Math.min(minY, part.y);
                maxY = Math.max(maxY, part.y + part.h);
            }
            assert minX == 0 && minY == 0 && maxX > 0 && maxY > 0 : "Parts must start at >= (0, 0)";
            float xW = maxX - minX;
            float yH = maxY - minY;
            maxSize *= scale;
            stack.translate(-maxSize / 2, -maxSize / 2, 1);
            renderInner(stack, entity, maxSize, yDiff, minX, minY, maxX, maxY, xW, yH);
        }

        public void renderInner(PoseStack stack, T entity, float maxSize, double fadeDiff, float minX, float minY, float maxX, float maxY, float xW, float yH) {
            RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableTexture();
            Matrix4f matrix = stack.last().pose();
            BufferBuilder builder = Tesselator.getInstance().getBuilder();
            boolean prevTexed = true;
            if (xW < yH) {
                float diff = (yH - xW) / 2;
                float scale = maxSize / yH;
                for (Part<T> part : parts) {
                    if (part instanceof TexturedPart<T> tp) {
                        builder.end();
                        BufferUploader.end(builder);
                        if (tp.bindTex(entity)) {
                            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
                            tp.render(matrix, builder, fadeDiff, diff, 0, scale, tp.texWidth, tp.texHeight);
                            builder.end();
                            BufferUploader.end(builder);
                        }
                        prevTexed = true;
                        continue;
                    } else if (prevTexed) {
                        bindTex(entity);
                        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
                        prevTexed = false;
                    }
                    part.render(matrix, builder, fadeDiff, diff, 0, scale, texWidth, texHeight);
                }
            } else {
                float diff = (xW - yH) / 2;
                float scale = maxSize / xW;
                for (Part<T> part : parts) {
                    if (part instanceof TexturedPart<T> tp) {
                        builder.end();
                        BufferUploader.end(builder);
                        if (tp.bindTex(entity)) {
                            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
                            tp.render(matrix, builder, fadeDiff, 0, diff, scale, tp.texWidth, tp.texHeight);
                            builder.end();
                            BufferUploader.end(builder);
                        }
                        prevTexed = true;
                        continue;
                    } else if (prevTexed) {
                        bindTex(entity);
                        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
                        prevTexed = false;
                    }
                    part.render(matrix, builder, fadeDiff, 0, diff, scale, texWidth, texHeight);
                }
            }
            if (!prevTexed) {
                builder.end();
                BufferUploader.end(builder);
            }
        }

        public void bindTex(T entity) {
            RenderSystem.setShaderTexture(
                0,
                minecraft.getEntityRenderDispatcher().getRenderer(entity).getTextureLocation(entity)
            );
        }

    }

    public static class Part<T extends LivingEntity> {
        public final float x;
        public final float y;
        public final float w;
        public final float h;
        public final float ux;
        public final float vy;
        public final float uw;
        public final float vh;

        public Part(float x, float y, float w, float h, float ux, float vy, float uw, float vh) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.ux = ux;
            this.vy = vy;
            this.uw = uw;
            this.vh = vh;
        }

        public void render(Matrix4f matrix, BufferBuilder builder, double fadeDiff, float xdiff, float ydiff, float scale, int texWidth, int texHeight) {
            int abgr;
            if (fadeDiff < 0) {
                int col = (int) ((1 + fadeDiff) * 0xFF);
                abgr = col << 24 | col << 16 | col << 8 | col;
            } else {
                int col = (int) ((1 - fadeDiff) * 0xFF);
                abgr = col << 24 | 0xFFFFFF;
            }
            drawTexCol(
                matrix,
                builder,
                (x + xdiff) * scale,
                (y + ydiff) * scale,
                w * scale,
                h * scale,
                ux / texWidth,
                vy / texHeight,
                (ux + uw) / texWidth,
                (vy + vh) / texHeight,
                abgr
            );
        }

        public static void drawTexCol(Matrix4f matrix, BufferBuilder builder, float x, float y, float width, float height, float startU, float startV, float endU, float endV, int abgr) {
            float a = (abgr >> 24 & 255) / 255f;
            float b = (abgr >> 16 & 255) / 255f;
            float g = (abgr >> 8 & 255) / 255f;
            float r = (abgr & 255) / 255f;
            builder.vertex(matrix, x, y + height, 0).color(r, g, b, a).uv(startU, endV).endVertex();
            builder.vertex(matrix, x + width, y + height, 0).color(r, g, b, a).uv(endU, endV).endVertex();
            builder.vertex(matrix, x + width, y, 0).color(r, g, b, a).uv(endU, startV).endVertex();
            builder.vertex(matrix, x, y, 0).color(r, g, b, a).uv(startU, startV).endVertex();
        }

    }

    public static abstract class TexturedPart<T extends LivingEntity> extends Part<T> {
        private final int texWidth;
        private final int texHeight;


        public TexturedPart(int texWidth, int texHeight, float x, float y, float w, float h, float ux, float vy, float uw, float vh) {
            super(x, y, w, h, ux, vy, uw, vh);
            this.texWidth = texWidth;
            this.texHeight = texHeight;
        }

        abstract boolean bindTex(T entity);

    }

    public static record Pair<T, U>(T t, U u) {
    }

}
