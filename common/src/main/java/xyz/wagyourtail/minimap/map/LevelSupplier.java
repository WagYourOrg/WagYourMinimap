package xyz.wagyourtail.minimap.map;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public abstract class LevelSupplier implements Supplier<String> {
    public final Supplier<Set<String>> availableLevels;

    // TODO: Bungeecord/Multiverse support somehow

    public LevelSupplier(Supplier<Set<String>> levels) {
        this.availableLevels = levels;
    }

}
