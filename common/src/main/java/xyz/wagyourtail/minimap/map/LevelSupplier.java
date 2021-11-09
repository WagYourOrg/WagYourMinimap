package xyz.wagyourtail.minimap.map;

import java.util.Set;
import java.util.function.Supplier;

public abstract class LevelSupplier implements Supplier<String> {
    public final Set<String> availableLevels;

    // TODO: Bungeecord/Multiverse support somehow

    public LevelSupplier(Set<String> levels) {
        this.availableLevels = levels;
    }

}
