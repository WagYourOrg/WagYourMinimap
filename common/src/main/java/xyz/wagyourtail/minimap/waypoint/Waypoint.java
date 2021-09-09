package xyz.wagyourtail.minimap.waypoint;

import com.google.gson.Gson;

public record Waypoint(int posX, int posY, int posZ, byte colR, byte colG, byte colB, String name, String[] groups,
                       String[] levels) {

    private static final Gson gson = new Gson();

    public static Waypoint deserialize(String inp) {
        return gson.fromJson(inp, Waypoint.class);
    }

    public String serialize() {
        return gson.toJson(this);
    }


}
