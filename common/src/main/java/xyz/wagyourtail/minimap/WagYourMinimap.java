package xyz.wagyourtail.minimap;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;

public abstract class WagYourMinimap {
    public static final String MOD_ID = "wagyourminimap";
    public static final Logger LOGGER = LoggerFactory.getLogger("WagYourMinimap");

    private static final MethodHandle GUI_GRAPHICS_CONSTRUCTOR;

    static {
        try {
            Constructor<GuiGraphics> guiGraphicsConstructor = GuiGraphics.class.getDeclaredConstructor(Minecraft.class, PoseStack.class, MultiBufferSource.BufferSource.class);
            guiGraphicsConstructor.setAccessible(true);
            GUI_GRAPHICS_CONSTRUCTOR = MethodHandles.lookup().unreflectConstructor(guiGraphicsConstructor);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static GuiGraphics constructGuiGraphics(Minecraft mc, PoseStack stack) {
        try {
            return (GuiGraphics) GUI_GRAPHICS_CONSTRUCTOR.invokeExact(mc, stack, mc.renderBuffers().bufferSource());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}
