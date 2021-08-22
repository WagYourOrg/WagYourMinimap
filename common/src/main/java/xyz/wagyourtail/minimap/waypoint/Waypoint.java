package xyz.wagyourtail.minimap.waypoint;

public record Waypoint(int posX, int posY, int posZ, int colR, int colG, int colB, String name, String[] groups,
                       String[] levels) {

//    public static Waypoint deserialize(String inp) {
//        //TODO;
//    }
}
