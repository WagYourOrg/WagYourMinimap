package xyz.wagyourtail.minimap.map;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.stream.Collectors;

public class VanillaLevelSupplier extends LevelSupplier {
    public static final Minecraft mc = Minecraft.getInstance();

    public VanillaLevelSupplier() {
        this(mc.getConnection());
    }

    protected VanillaLevelSupplier(ClientPacketListener listener) {
        super(ImmutableSet.copyOf(listener == null ?
            ImmutableSet.of() :
            listener.levels().stream().map(VanillaLevelSupplier::getLevelName).collect(Collectors.toSet())));
    }

    public static String getLevelName(ResourceKey<Level> dimension) {
        return dimension.location().toString().replace(":", "/");
    }

    @Override
    public String get() {
        assert mc.level != null;
        return getLevelName(mc.level);
    }

    public static String getLevelName(Level level) {
        return getLevelName(level.dimension());
    }

}
