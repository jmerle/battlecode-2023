package camel_case_v22_international_qualifiers.util;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public enum Symmetry {
    ROTATIONAL(true, true),
    VERTICAL(true, false),
    HORIZONTAL(false, true);

    private final boolean reflectX;
    private final boolean reflectY;

    Symmetry(boolean reflectX, boolean reflectY) {
        this.reflectX = reflectX;
        this.reflectY = reflectY;
    }

    public MapLocation reflect(RobotController rc, MapLocation location) {
        int x = reflectX ? reflect(location.x, rc.getMapWidth() / 2) : location.x;
        int y = reflectY ? reflect(location.y, rc.getMapHeight() / 2) : location.y;

        return new MapLocation(x, y);
    }

    private int reflect(int value, int middle) {
        if (value < middle) {
            return middle + Math.abs(value - middle) - 1;
        } else {
            return middle - Math.abs(value - middle) - 1;
        }
    }
}
