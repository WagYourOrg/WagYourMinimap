package xyz.wagyourtail.minimap.client.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import xyz.wagyourtail.config.gui.widgets.NamedEditBox;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.waypoint.Waypoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class WaypointEditScreen extends Screen {
    private final Screen parent;
    private final Waypoint prev_point;

    protected EditBox name;
    protected EditBox posX;
    protected EditBox posY;
    protected EditBox posZ;
    protected EditBox color;
    protected EditBox groups;
    protected EditBox dims;
    protected Set<String> realDims = MinimapApi.getInstance().getMapServer().getAvailableLevels();
    protected List<Component> sideText;
    protected boolean canceled = false;

    public WaypointEditScreen(Screen parent, Waypoint prev_point) {
        super(new TranslatableComponent("gui.wagyourminimap.waypoint_edit"));
        this.parent = parent;
        this.prev_point = prev_point;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(poseStack);
        drawCenteredString(poseStack, font, title, width / 2, 17, 0xFFFFFF);
        int i = 5;
        for (Component comp : sideText) {
            font.draw(poseStack, comp, 5, i, 0xFFFFFF);
            i += 9;
        }
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    public Waypoint compileWaypoint() {
        String name = this.name.getValue();

        int posX = Integer.parseInt(this.posX.getValue());
        int posY = Integer.parseInt(this.posY.getValue());
        int posZ = Integer.parseInt(this.posZ.getValue());

        int color = Integer.parseInt(this.color.getValue(), 16);

        String[] groups = Arrays.stream(this.groups.getValue().split(",")).map(String::trim).toArray(String[]::new);
        String[] dims = Arrays.stream(this.dims.getValue().split(",")).map(e -> e.trim().replace(":", "_")).filter(realDims::contains).toArray(String[]::new);

        return new Waypoint(posX, posY, posZ, (byte)((color >> 16) & 255), (byte)((color >> 8) & 255), (byte)(color & 255), name, groups, dims, false);
    }

    @Override
    public void onClose() {
        MinimapApi.getInstance().getMapServer().waypoints.removeWaypoint(prev_point);
        if (!canceled) MinimapApi.getInstance().getMapServer().waypoints.addWaypoint(compileWaypoint());
        minecraft.setScreen(parent);
    }

    @Override
    protected void init() {
        super.init();
        int h = Math.max(height / 2 - 100, 30);
        name = addRenderableWidget(new NamedEditBox(font, width / 2 - 200, h, 400, 20, new TranslatableComponent("gui.wagyourminimap.name")));
        name.setValue(prev_point.name());

        posX = addRenderableWidget(new NamedEditBox(font, width / 2 - 200, h + 25, 60, 20, new TextComponent("x")));
        posX.setFilter(s -> s.matches("-?\\d*"));
        posX.setValue(Integer.toString(prev_point.posX()));

        posY = addRenderableWidget(new NamedEditBox(font, width / 2 - 134, h + 25, 60, 20, new TextComponent("y")));
        posY.setFilter(s -> s.matches("-?\\d*"));
        posY.setValue(Integer.toString(prev_point.posY()));

        posZ = addRenderableWidget(new NamedEditBox(font, width / 2 - 68, h + 25, 60, 20, new TextComponent("z")));
        posZ.setFilter(s -> s.matches("-?\\d*"));
        posZ.setValue(Integer.toString(prev_point.posZ()));

        color = addRenderableWidget(new NamedEditBox(font, width / 2, h + 25, 200, 20, new TranslatableComponent("gui.wagyourminimap.color")));
        color.setFilter((s) -> s.matches("[\\da-fA-F]{0,6}"));
        color.setValue(zeroPad(Integer.toHexString(prev_point.colR() & 255)) + zeroPad(Integer.toHexString(prev_point.colG() & 255)) + zeroPad(Integer.toHexString(prev_point.colB() & 255)));

        int gradientHeight = Math.max(Math.min(100, height - h - 130), 0);
        //TODO: finish and use ColorButton (gradient color picker)

        groups = addRenderableWidget(new NamedEditBox(font, width / 2 - 200, h + gradientHeight + 50, 400, 20, new TranslatableComponent("gui.wagyourminimap.groups")));
        groups.setMaxLength(Integer.MAX_VALUE);
        groups.setValue(String.join(", ", prev_point.groups()));

        dims = addRenderableWidget(new NamedEditBox(font, width / 2 - 200, h + gradientHeight + 75, 400, 20, new TranslatableComponent("gui.wagyourminimap.levels")));
        dims.setMaxLength(Integer.MAX_VALUE);
        dims.setValue(String.join(", ", prev_point.levels()));

        addRenderableWidget(new Button(width / 2 - 100, h + gradientHeight + 100, 95, 20, new TranslatableComponent("gui.wagyourminimap.cancel"), (btn) -> {
            canceled = true;
            this.onClose();
        }));

        addRenderableWidget(new Button(width / 2 + 5, h + gradientHeight + 100, 95, 20, new TranslatableComponent("gui.wagyourminimap.save"), (btn) -> {
            this.onClose();
        }));

        sideText = new ArrayList<>();
        if (width > 600) {
            sideText.add(new TranslatableComponent("gui.wagyourminimap.player_pos"));
            sideText.add(new TextComponent(minecraft.player.blockPosition().toShortString()));
            sideText.add(new TextComponent(""));
            sideText.add(new TranslatableComponent("gui.wagyourminimap.dimensions_list").append(": "));
            realDims.stream().sorted().forEach(e -> sideText.add(new TextComponent(e)));
        }


        setFocused(name);
    }

    public String zeroPad(String in) {
        if (in.length() == 1) {
            return "0" + in;
        }
        return in;
    }

}
