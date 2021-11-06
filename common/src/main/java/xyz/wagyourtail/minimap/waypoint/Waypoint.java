package xyz.wagyourtail.minimap.waypoint;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import xyz.wagyourtail.minimap.map.MapServer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class Waypoint {
    public final double coordScale;
    public final int posX;
    public final int posY;
    public final int posZ;
    public final byte colR;
    public final byte colG;
    public final byte colB;
    public final String name;
    public final String[] groups;
    public final String[] levels;
    public final JsonObject extra;
    public boolean enabled;
    public boolean ephemeral;


    public Waypoint(double coordScale, int posX, int posY, int posZ, byte colR, byte colG, byte colB, String name, String[] groups,
                    String[] levels, JsonObject extra, boolean enabled, boolean ephemeral) {
        this.coordScale = coordScale;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.colR = colR;
        this.colG = colG;
        this.colB = colB;
        this.name = name;
        this.groups = groups;
        this.levels = levels;
        this.extra = extra;
        this.enabled = enabled;
        this.ephemeral = ephemeral;
    }

    private static final Gson gson = new Gson();
    private final Map<Double, BlockPos> posForCoordScale = new HashMap<>();

    public static <T> T getKeyOrDefault(JsonObject element, String name, Function<JsonElement, T> key, T def) {
        return element.has(name) ? key.apply(element.get(name)) : def;
    }

    public static Waypoint deserialize(String inp) {
        JsonObject waypoint = new JsonParser().parse(inp).getAsJsonObject();
        return new Waypoint(
            getKeyOrDefault(waypoint, "coordScale", JsonElement::getAsDouble, 1.0),
            getKeyOrDefault(waypoint, "posX", JsonElement::getAsInt, 0),
            getKeyOrDefault(waypoint, "posY", JsonElement::getAsInt, 0),
            getKeyOrDefault(waypoint, "posZ", JsonElement::getAsInt, 0),
            getKeyOrDefault(waypoint, "colR", JsonElement::getAsByte, (byte) 0),
            getKeyOrDefault(waypoint, "colG", JsonElement::getAsByte, (byte) 0),
            getKeyOrDefault(waypoint, "colB", JsonElement::getAsByte, (byte) 0),
            getKeyOrDefault(waypoint, "name", JsonElement::getAsString, ""),
            waypoint.has("groups") ? gson.fromJson(waypoint.get("groups"), String[].class) : new String[0],
            waypoint.has("levels") ? gson.fromJson(waypoint.get("levels"), String[].class) : new String[]{MapServer.getLevelName(
                Level.OVERWORLD), MapServer.getLevelName(Level.NETHER)},
            getKeyOrDefault(waypoint, "extra", JsonElement::getAsJsonObject, new JsonObject()),
            getKeyOrDefault(waypoint, "enabled", JsonElement::getAsBoolean, true),
            false
        );
    }

    public BlockPos posForCoordScale(double coordScale) {
        return posForCoordScale.computeIfAbsent(coordScale, k -> {
            double scale = this.coordScale / coordScale;
            return new BlockPos(posX * scale, posY, posZ * scale);
        });
    }

    public String serialize() {
        return gson.toJson(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Waypoint waypoint)) return false;
        return posX == waypoint.posX && posY == waypoint.posY && posZ == waypoint.posZ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(posX, posY, posZ);
    }

}
