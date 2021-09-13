package xyz.wagyourtail.minimap.waypoint;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Objects;

public record Waypoint(int posX, int posY, int posZ, byte colR, byte colG, byte colB, String name, String[] groups,
                       String[] levels, boolean ephemeral) {

    private static final Gson gson = new Gson();

    public static Waypoint deserialize(String inp) {
        JsonObject waypoint = new JsonParser().parse(inp).getAsJsonObject();
        return new Waypoint(
            waypoint.get("posX").getAsInt(),
            waypoint.get("posY").getAsInt(),
            waypoint.get("posZ").getAsInt(),
            waypoint.get("colR").getAsByte(),
            waypoint.get("colG").getAsByte(),
            waypoint.get("colB").getAsByte(),
            waypoint.get("name").getAsString(),
            gson.fromJson(waypoint.get("groups"), String[].class),
            gson.fromJson(waypoint.get("levels"), String[].class),
            false
        );
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
