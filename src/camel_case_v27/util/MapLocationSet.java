package camel_case_v27.util;

import battlecode.common.MapLocation;

public class MapLocationSet {
    private StringBuilder sb = new StringBuilder();

    public void add(MapLocation location) {
        sb.append((char) (location.y * 60 + location.x));
    }

    public boolean contains(MapLocation location) {
        return sb.indexOf(String.valueOf((char) (location.y * 60 + location.x))) != -1;
    }
}
