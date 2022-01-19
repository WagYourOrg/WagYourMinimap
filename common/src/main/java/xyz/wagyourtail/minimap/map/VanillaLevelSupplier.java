package xyz.wagyourtail.minimap.map;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Set;
import java.util.stream.Collectors;

public class VanillaLevelSupplier extends LevelSupplier {
    public static final Minecraft mc = Minecraft.getInstance();

    protected VanillaLevelSupplier() {
    }

    @Override
    public String getLevelName(Level level) {
        return getLevelName(level.dimension());
    }

    public static String getLevelName(ResourceKey<Level> dimension) {
        return dimension.location().toString().replace(":", "/");
    }

    @Override
    public Set<String> getAvailableLevels() {
        ClientPacketListener listener = mc.getConnection();
        if (listener == null) {
            return ImmutableSet.of();
        }
        return listener.levels().stream().map(VanillaLevelSupplier::getLevelName).collect(Collectors.toSet());
    }

}
