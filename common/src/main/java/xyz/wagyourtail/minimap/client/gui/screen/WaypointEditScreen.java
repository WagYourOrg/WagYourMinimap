package xyz.wagyourtail.minimap.client.gui.screen;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import xyz.wagyourtail.config.gui.widgets.NamedEditBox;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.client.gui.screen.widget.ColorButton;
import xyz.wagyourtail.minimap.waypoint.Waypoint;

import java.awt.*;
import java.util.List;
import java.util.*;

public class WaypointEditScreen extends Screen {
    private static final Random random = new Random();
    private final static Gson gson = new Gson();
    private final Screen parent;
    private final Waypoint prev_point;

    protected EditBox name;
    protected EditBox coordScale;
    protected EditBox posX;
    protected EditBox posY;
    protected EditBox posZ;
    protected EditBox color;
    protected ColorButton colorSelector;
    protected EditBox groups;
    protected EditBox dims;
    protected EditBox extra;
    protected Set<String> realDims = MinimapApi.getInstance()
        .getMapServer().levelNameSupplier.getAvailableLevels();
    protected List<Component> sideText;
    protected boolean canceled = false;

    public WaypointEditScreen(Screen parent, Waypoint prev_point) {
        super(new TranslatableComponent("gui.wagyourminimap.waypoint_edit"));
        this.parent = parent;
        this.prev_point = prev_point;
    }

    public static WaypointEditScreen createNewFromPos(Screen parent, BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        int color = Color.HSBtoRGB(random.nextFloat(), 1f, 1f);
        String[] dims;
        if (mc.level.dimension().equals(Level.OVERWORLD) || mc.level.dimension().equals(Level.NETHER)) {
            dims = new String[] {"minecraft/overworld", "minecraft/the_nether"};
        } else {
            dims = new String[] {MinimapApi.getInstance().getMapServer().getLevelFor(mc.level).level_slug()};
        }

        return new WaypointEditScreen(parent, new Waypoint(
            mc.level.dimensionType().coordinateScale(),
            pos.getX(),
            pos.getY(),
            pos.getZ(),
            (byte) (color >> 16 & 0xFF),
            (byte) (color >> 8 & 0xFF),
            (byte) (color & 0xFF),
            "",
            new String[] {"default"},
            dims,
            new JsonObject(),
            "default",
            true,
            false
        ));
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

    @Override
    public void onClose() {
        if (!canceled) {
            MinimapApi.getInstance().getMapServer().waypoints.removeWaypoint(prev_point);
            MinimapApi.getInstance().getMapServer().waypoints.addWaypoint(compileWaypoint());
        }
        assert minecraft != null;
        minecraft.setScreen(parent);
    }

    public Waypoint compileWaypoint() {
        String name = this.name.getValue();

        double coordScale = Double.parseDouble(this.coordScale.getValue());

        int posX = Integer.parseInt(this.posX.getValue());
        int posY = Integer.parseInt(this.posY.getValue());
        int posZ = Integer.parseInt(this.posZ.getValue());

        int color = Integer.parseInt(this.color.getValue(), 16);

        String[] groups = Arrays.stream(this.groups.getValue().split(",")).map(String::trim).toArray(String[]::new);
        String[] dims = Arrays.stream(this.dims.getValue().split(",")).map(e -> e.trim().replace(":", "/")).filter(
            realDims::contains).toArray(String[]::new);

        JsonObject extra;
        try {
            extra = new JsonParser().parse(this.extra.getValue()).getAsJsonObject();
        } catch (Exception e) {
            extra = new JsonObject();
        }

        return new Waypoint(
            coordScale,
            posX,
            posY,
            posZ,
            (byte) (color >> 16 & 255),
            (byte) (color >> 8 & 255),
            (byte) (color & 255),
            name,
            groups,
            dims,
            extra,
            "default",
            true,
            false
        );
    }

    @Override
    protected void init() {
        super.init();
        int h = Math.max(height / 2 - 100, 30);
        name = addRenderableWidget(new NamedEditBox(
            font,
            width / 2 - 200,
            h,
            400,
            20,
            new TranslatableComponent("gui.wagyourminimap.name")
        ));
        name.setValue(prev_point.name);

        coordScale = addRenderableWidget(new NamedEditBox(
            font,
            width / 2 - 200,
            h + 25,
            60,
            20,
            new TranslatableComponent("gui.wagyourminimap.coord_scale")
        ));
        coordScale.setFilter(s -> s.matches("-?\\d*.\\d*"));
        coordScale.setValue(Double.toString(prev_point.coordScale));

        posX = addRenderableWidget(new NamedEditBox(font, width / 2 - 134, h + 25, 60, 20, new TextComponent("x")));
        posX.setFilter(s -> s.matches("-?\\d*"));
        posX.setValue(Integer.toString(prev_point.posX));

        posY = addRenderableWidget(new NamedEditBox(font, width / 2 - 68, h + 25, 60, 20, new TextComponent("y")));
        posY.setFilter(s -> s.matches("-?\\d*"));
        posY.setValue(Integer.toString(prev_point.posY));

        posZ = addRenderableWidget(new NamedEditBox(font, width / 2 - 2, h + 25, 60, 20, new TextComponent("z")));
        posZ.setFilter(s -> s.matches("-?\\d*"));
        posZ.setValue(Integer.toString(prev_point.posZ));

        color = addRenderableWidget(new NamedEditBox(
            font,
            width / 2 + 64,
            h + 25,
            136,
            20,
            new TranslatableComponent("gui.wagyourminimap.color")
        ));
        color.setFilter((s) -> s.matches("[\\da-fA-F]{0,6}"));
        color.setValue(
            zeroPad(Integer.toHexString(prev_point.colR & 255)) + zeroPad(Integer.toHexString(prev_point.colG & 255)) +
                zeroPad(Integer.toHexString(prev_point.colB & 255)));
        int prevPointColor = ((prev_point.colR & 255) << 16) | ((prev_point.colG & 255) << 8) | (prev_point.colB & 255);
        int gradientHeight = Math.max(Math.min(100, height - h - 155), 0);
        colorSelector = addRenderableWidget(new ColorButton(
            width / 2 - 200,
            h + 50,
            400,
            gradientHeight - 10,
            prevPointColor,
            color -> {
                int r = (color >> 16) & 255;
                int g = (color >> 8) & 255;
                int b = color & 255;
                this.color.setResponder(null);
                this.color.setValue(zeroPad(Integer.toHexString(r)) + zeroPad(Integer.toHexString(g)) +
                    zeroPad(Integer.toHexString(b)));
                this.color.setResponder(s -> {
                    int col = s.isEmpty() ? 0 : Integer.parseInt(s, 16);
                    colorSelector.setCurrentColor(col);
                });
            }
        ));
        color.setResponder(s -> {
            int col = s.isEmpty() ? 0 : Integer.parseInt(s, 16);
            colorSelector.setCurrentColor(col);
        });

        groups = addRenderableWidget(new NamedEditBox(
            font,
            width / 2 - 200,
            h + gradientHeight + 50,
            400,
            20,
            new TranslatableComponent("gui.wagyourminimap.groups")
        ));
        groups.setMaxLength(Integer.MAX_VALUE);
        groups.setValue(String.join(", ", prev_point.groups));

        dims = addRenderableWidget(new NamedEditBox(
            font,
            width / 2 - 200,
            h + gradientHeight + 75,
            400,
            20,
            new TranslatableComponent("gui.wagyourminimap.levels")
        ));
        dims.setMaxLength(Integer.MAX_VALUE);
        dims.setValue(String.join(", ", prev_point.levels));

        extra = addRenderableWidget(new NamedEditBox(
            font,
            width / 2 - 200,
            h + gradientHeight + 100,
            400,
            20,
            new TranslatableComponent("gui.wagyourminimap.extra")
        ));
        extra.setMaxLength(Integer.MAX_VALUE);
        extra.setValue(gson.toJson(prev_point.extra));

        addRenderableWidget(new Button(
            width / 2 - 100,
            h + gradientHeight + 125,
            95,
            20,
            new TranslatableComponent("gui.wagyourminimap.cancel"),
            (btn) -> {
                canceled = true;
                this.onClose();
            }
        ));

        addRenderableWidget(new Button(
            width / 2 + 5,
            h + gradientHeight + 125,
            95,
            20,
            new TranslatableComponent("gui.wagyourminimap.save"),
            (btn) -> {
                this.onClose();
            }
        ));

        sideText = new ArrayList<>();
        if (width > 600) {
            sideText.add(new TranslatableComponent("gui.wagyourminimap.player_pos"));
            sideText.add(new TextComponent(minecraft.cameraEntity.blockPosition().toShortString()));
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
