package camel_case_v03.util;

import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class SharedArray {
    public static final int MAX_DANGER_TARGETS = 20;

    private static final int HQ_LOCATION_OFFSET = 1;
    private static final int DANGER_TARGET_OFFSET = HQ_LOCATION_OFFSET + GameConstants.MAX_STARTING_HEADQUARTERS;

    private RobotController rc;

    public SharedArray(RobotController rc) {
        this.rc = rc;
    }

    public boolean canWrite() {
        return rc.canWriteSharedArray(0, 0);
    }

    public int getHeadquartersTurnIndex() throws GameActionException {
        int value = rc.readSharedArray(0);

        int roundOffset = rc.getRoundNum() * GameConstants.MAX_STARTING_HEADQUARTERS;
        if (value < roundOffset) {
            write(0, roundOffset);
            return 0;
        } else {
            write(0, value + 1);
            return value - roundOffset + 1;
        }
    }

    public int getHqCount() throws GameActionException {
        for (int i = 0; i < GameConstants.MAX_STARTING_HEADQUARTERS; i++) {
            if (rc.readSharedArray(HQ_LOCATION_OFFSET + i) == 0) {
                return i;
            }
        }

        return GameConstants.MAX_STARTING_HEADQUARTERS;
    }

    public MapLocation getMyHqLocation(int hqIndex) throws GameActionException {
        return intToLocation(rc.readSharedArray(HQ_LOCATION_OFFSET + hqIndex));
    }

    public void setMyHqLocation(int hqIndex, MapLocation location) throws GameActionException {
        write(HQ_LOCATION_OFFSET + hqIndex, locationToInt(location));
    }

    public MapLocation getDangerTarget(int index) throws GameActionException {
        int value = rc.readSharedArray(DANGER_TARGET_OFFSET + index);
        return value > 0 ? intToLocation(value % 5_000) : null;
    }

    public void addDangerTarget(MapLocation location, int expiration) throws GameActionException {
        for (int i = 0; i < SharedArray.MAX_DANGER_TARGETS; i++) {
            MapLocation dangerTarget = getDangerTarget(i);
            if (location.equals(dangerTarget)) {
                write(DANGER_TARGET_OFFSET + i, locationToInt(location) + expiration * 5_000);
                return;
            }
        }

        for (int i = 0; i < SharedArray.MAX_DANGER_TARGETS; i++) {
            MapLocation dangerTarget = getDangerTarget(i);
            if (dangerTarget == null) {
                write(DANGER_TARGET_OFFSET + i, locationToInt(location) + expiration * 5_000);
                return;
            }
        }
    }

    public void expireDangerTargets() throws GameActionException {
        for (int i = 0; i < MAX_DANGER_TARGETS; i++) {
            int value = rc.readSharedArray(DANGER_TARGET_OFFSET + i);
            if (value > 5_000) {
                write(DANGER_TARGET_OFFSET + i, value - 5_000);
            } else if (value > 0) {
                write(DANGER_TARGET_OFFSET + i, 0);
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
