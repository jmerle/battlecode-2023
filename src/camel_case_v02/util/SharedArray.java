package camel_case_v02.util;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class SharedArray {
    public static final int MAX_DANGER_TARGETS = 20;

    private RobotController rc;

    public SharedArray(RobotController rc) {
        this.rc = rc;
    }

    public boolean canWrite() {
        return rc.canWriteSharedArray(0, 0);
    }

    public int getHeadquartersTurnIndex() throws GameActionException {
        int value = rc.readSharedArray(0);

        int roundOffset = rc.getRoundNum() * 10;
        if (value < roundOffset) {
            write(0, roundOffset);
            return 0;
        } else {
            write(0, value + 1);
            return value - roundOffset + 1;
        }
    }

    public MapLocation getDangerTarget(int index) throws GameActionException {
        int value = rc.readSharedArray(index + 1);
        return value > 0 ? intToLocation(value % 5_000) : null;
    }

    public void addDangerTarget(MapLocation location, int expiration) throws GameActionException {
        for (int i = 0; i < SharedArray.MAX_DANGER_TARGETS; i++) {
            MapLocation dangerTarget = getDangerTarget(i);
            if (location.equals(dangerTarget)) {
                write(i + 1, locationToInt(location) + expiration * 5_000);
                return;
            }
        }

        for (int i = 0; i < SharedArray.MAX_DANGER_TARGETS; i++) {
            MapLocation dangerTarget = getDangerTarget(i);
            if (dangerTarget == null) {
                write(i + 1, locationToInt(location) + expiration * 5_000);
                return;
            }
        }
    }

    public void expireDangerTargets() throws GameActionException {
        for (int i = 0; i < MAX_DANGER_TARGETS; i++) {
            int value = rc.readSharedArray(i + 1);
            if (value > 5_000) {
                write(i + 1, value - 5_000);
            } else if (value > 0) {
                write(i + 1, 0);
            }
        }
    }

    private int locationToInt(MapLocation location) {
        return (location.y * 60 + location.x) + 1;
    }

    private MapLocation intToLocation(int value) {
        return new MapLocation((value - 1) % 60, (value - 1) / 60);
    }

    private void write(int index, int newValue) throws GameActionException {
        if (rc.readSharedArray(index) != newValue) {
            rc.writeSharedArray(index, newValue);
        }
    }
}
