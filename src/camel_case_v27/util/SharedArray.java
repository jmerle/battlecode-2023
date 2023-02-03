package camel_case_v27.util;

import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.ArrayList;
import java.util.List;

public class SharedArray {
    private static final int HQ_LOCATION_OFFSET = 1;
    private static final int WELLS_OFFSET = HQ_LOCATION_OFFSET + GameConstants.MAX_STARTING_HEADQUARTERS;
    private static final int MAX_WELLS_PER_HQ = 5;

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

    public List<MapLocation> getManaWellLocations(MapLocation hqLocation) throws GameActionException {
        List<MapLocation> wells = new ArrayList<>();

        int hqIndex = hqLocationToIndex(hqLocation);
        for (int i = 0; i < MAX_WELLS_PER_HQ; i++) {
            int value = rc.readSharedArray(WELLS_OFFSET + hqIndex * GameConstants.MAX_STARTING_HEADQUARTERS + i);
            if (value == 0) {
                break;
            }

            wells.add(intToLocation(value));
        }

        return wells;
    }

    public void setManaWellLocation(MapLocation hqLocation, MapLocation well) throws GameActionException {
        int hqIndex = hqLocationToIndex(hqLocation);

        for (int i = 0; i < MAX_WELLS_PER_HQ; i++) {
            int value = rc.readSharedArray(WELLS_OFFSET + hqIndex * GameConstants.MAX_STARTING_HEADQUARTERS + i);

            if (value == 0) {
                write(WELLS_OFFSET + hqIndex * GameConstants.MAX_STARTING_HEADQUARTERS + i, locationToInt(well));
                break;
            }

            MapLocation location = intToLocation(value);
            if (location.equals(well)) {
                break;
            }
        }
    }

    private int locationToInt(MapLocation location) {
        return (location.y * 60 + location.x) + 1;
    }

    private MapLocation intToLocation(int value) {
        return new MapLocation((value - 1) % 60, (value - 1) / 60);
    }

    private int hqLocationToIndex(MapLocation location) throws GameActionException {
        for (int i = 0; i < GameConstants.MAX_STARTING_HEADQUARTERS; i++) {
            if (getMyHqLocation(i).equals(location)) {
                return i;
            }
        }

        return -1;
    }

    private void write(int index, int newValue) throws GameActionException {
        if (rc.readSharedArray(index) != newValue) {
            rc.writeSharedArray(index, newValue);
        }
    }
}
