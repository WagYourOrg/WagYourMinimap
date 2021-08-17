package xyz.wagyourtail.minimap.api.client;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.EnumSelectorBuilder;
import me.shedaniel.clothconfig2.impl.builders.IntSliderBuilder;
import me.shedaniel.clothconfig2.impl.builders.LongSliderBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import xyz.wagyourtail.minimap.api.WagYourMinimapConfig;
import xyz.wagyourtail.minimap.client.gui.InGameHud;

public class WagYourMinimapClientConfig extends WagYourMinimapConfig {

    private static final ConfigEntryBuilder configEntryBuilder = ConfigEntryBuilder.create();

    public int minimapChunkRadius = 1;

    public InGameHud.SnapSide snapSide = InGameHud.SnapSide.TOP_RIGHT;
    public float mapScreenPercent = 0.30F;

    public Screen getConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(new TranslatableComponent("gui.wagyourminimap.config_title"));

        ConfigCategory generalCategory = builder.getOrCreateCategory(new TranslatableComponent("stat.generalButton"));

        LongSliderBuilder regionCacheSize = configEntryBuilder.startLongSlider(new TranslatableComponent("config.wagyourminimap.region_cache_size"), this.regionCacheSize, 0, 2000L);
        regionCacheSize.setSaveConsumer(size ->
            this.regionCacheSize = size
        );
        generalCategory.addEntry(regionCacheSize.build());

        IntSliderBuilder minimapChunkRadius = configEntryBuilder.startIntSlider(new TranslatableComponent("config.wagyourminimap.minimap_chunk_radius"), this.minimapChunkRadius, 1, 32);
        minimapChunkRadius.setSaveConsumer(radius ->
            this.minimapChunkRadius = radius
        );
        generalCategory.addEntry(minimapChunkRadius.build());

        EnumSelectorBuilder<InGameHud.SnapSide> snapSide = configEntryBuilder.startEnumSelector(new TranslatableComponent("config.wagyourminimap.snap"), InGameHud.SnapSide.class, this.snapSide);
        snapSide.setSaveConsumer(snap ->
            this.snapSide = snap
        );
        generalCategory.addEntry(snapSide.build());

        IntSliderBuilder mapScreenPercent = configEntryBuilder.startIntSlider(new TranslatableComponent("config.wagyourminimap.size"), (int) (this.mapScreenPercent * 100), 0, 100);
        mapScreenPercent.setSaveConsumer(size ->
            this.mapScreenPercent = size / 100F
        );
        generalCategory.addEntry(mapScreenPercent.build());


        builder.setSavingRunnable(MinimapClientApi.getInstance()::saveConfig);

        return builder.build();
    }

}
