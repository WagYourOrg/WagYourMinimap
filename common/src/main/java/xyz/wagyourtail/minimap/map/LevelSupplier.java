package xyz.wagyourtail.minimap.map;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public abstract class LevelSupplier implements Supplier<String> {
    public abstract Set<String> getAvailableLevels();
}
