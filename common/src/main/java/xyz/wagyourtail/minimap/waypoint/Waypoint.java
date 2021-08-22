package xyz.wagyourtail.minimap.waypoint;

public record Waypoint(int posX, int posY, int posZ, byte colR, byte colG, byte colB, String name, String[] groups,
                       String[] levels) {

//    public static Waypoint deserialize(String inp) {
//        //TODO;
//    }
}
