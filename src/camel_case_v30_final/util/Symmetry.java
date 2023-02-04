package camel_case_v30_final.util;

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
        int x = reflectX ? (rc.getMapWidth() - 1 - location.x) : location.x;
        int y = reflectY ? (rc.getMapHeight() - 1 - location.y) : location.y;

        return new MapLocation(x, y);
    }
}
