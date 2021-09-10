package xyz.wagyourtail.minimap.waypoint;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public record Waypoint(int posX, int posY, int posZ, byte colR, byte colG, byte colB, String name, String[] groups,
                       String[] levels) {

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
            gson.fromJson(waypoint.get("levels"), String[].class)
        );
    }

    public String serialize() {
        return gson.toJson(this);
    }


}
