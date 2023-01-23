package camel_case_v07_sprint1_sprint2.robot;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.ResourceType;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Headquarters extends Robot {
    private boolean isFirstRun = true;

    public Headquarters(RobotController rc) {
        super(rc, RobotType.HEADQUARTERS);
    }

    @Override
    public void run() throws GameActionException {
        int turnIndex = sharedArray.getHeadquartersTurnIndex();

        if (isFirstRun) {
            sharedArray.setMyHqLocation(turnIndex, rc.getLocation());

            isFirstRun = false;
        }

        if (!rc.isActionReady()) {
            return;
        }

        RobotType type = hasResources(RobotType.LAUNCHER) ? RobotType.LAUNCHER : RobotType.CARRIER;
        if (!hasResources(type)) {
            return;
        }

        boolean minimizeDistanceToCenter = type == RobotType.LAUNCHER;
        MapLocation center = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2);

        MapLocation bestLocation = null;
        int bestDistanceToCenter = minimizeDistanceToCenter ? Integer.MAX_VALUE : Integer.MIN_VALUE;

        for (MapLocation location : rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), me.actionRadiusSquared)) {
            if (!rc.canBuildRobot(type, location)) {
                continue;
            }

            int distanceToCenter = location.distanceSquaredTo(center);
            if ((minimizeDistanceToCenter && distanceToCenter < bestDistanceToCenter)
                    || (!minimizeDistanceToCenter && distanceToCenter > bestDistanceToCenter)) {
                bestLocation = location;
                bestDistanceToCenter = distanceToCenter;
            }
        }

        if (bestLocation != null) {
            tryBuildRobot(type, bestLocation);
        }
    }

    private boolean hasResources(RobotType type) {
        for (ResourceType resource : resourceTypes) {
            if (rc.getResourceAmount(resource) < type.getBuildCost(resource)) {
                return false;
            }
        }

        return true;
    }

    private boolean tryBuildRobot(RobotType type, MapLocation location) throws GameActionException {
        if (rc.canBuildRobot(type, location)) {
            rc.buildRobot(type, location);
            return true;
        }

        return false;
    }
}
