package camel_case.robot;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.ResourceType;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.WellInfo;

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

        if (rc.getRoundNum() > 1750) {
            return;
        }

        if (!rc.isActionReady()) {
            return;
        }

        RobotType type = hasResources(RobotType.LAUNCHER) ? RobotType.LAUNCHER : RobotType.CARRIER;
        if (!hasResources(type)) {
            return;
        }

        MapLocation location = null;

        if (type == RobotType.CARRIER) {
            location = getBuildLocationNearWell(ResourceType.MANA);
            if (location == null) {
                location = getBuildLocationNearWell(ResourceType.ADAMANTIUM);
            }
        }

        if (location == null) {
            location = getBuildLocationRelativeToCenter(type, type == RobotType.LAUNCHER);
        }

        if (location != null) {
            tryBuildRobot(type, location);
        }
    }

    private MapLocation getBuildLocationRelativeToCenter(RobotType type, boolean minimizeDistanceToCenter) throws GameActionException {
        MapLocation center = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2);

        MapLocation bestLocation = null;
        int bestDistance = minimizeDistanceToCenter ? Integer.MAX_VALUE : Integer.MIN_VALUE;

        for (MapLocation location : rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), me.actionRadiusSquared)) {
            if (!rc.canBuildRobot(type, location)) {
                continue;
            }

            int distance = location.distanceSquaredTo(center);
            if ((minimizeDistanceToCenter && distance < bestDistance) || (!minimizeDistanceToCenter && distance > bestDistance)) {
                bestLocation = location;
                bestDistance = distance;
            }
        }

        return bestLocation;
    }

    private MapLocation getBuildLocationNearWell(ResourceType resource) throws GameActionException {
        WellInfo[] wells = rc.senseNearbyWells(resource);
        if (wells.length == 0) {
            return null;
        }

        MapLocation bestLocation = null;
        int bestDistance = Integer.MAX_VALUE;

        for (MapLocation location : rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), me.actionRadiusSquared)) {
            if (!rc.canBuildRobot(RobotType.CARRIER, location)) {
                continue;
            }

            int distance = Integer.MAX_VALUE;
            for (WellInfo well : wells) {
                distance = Math.min(distance, well.getMapLocation().distanceSquaredTo(location));
            }

            if (distance < bestDistance) {
                bestLocation = location;
                bestDistance = distance;
            }
        }

        return bestLocation;
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
