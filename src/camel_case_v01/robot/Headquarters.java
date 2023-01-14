package camel_case_v01.robot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.ResourceType;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Headquarters extends Robot {
    public Headquarters(RobotController rc) {
        super(rc, RobotType.HEADQUARTERS);
    }

    @Override
    public void run() throws GameActionException {
        if (!rc.isActionReady()) {
            return;
        }

        RobotType type = hasResources(RobotType.LAUNCHER) && canSeeFriendlyCarrier()
                ? RobotType.LAUNCHER
                : RobotType.CARRIER;

        for (Direction direction : adjacentDirections) {
            if (tryBuildRobot(type, rc.adjacentLocation(direction))) {
                return;
            }
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

    private boolean canSeeFriendlyCarrier() throws GameActionException {
        for (RobotInfo robot : rc.senseNearbyRobots(me.visionRadiusSquared, myTeam)) {
            if (robot.type == RobotType.CARRIER) {
                return true;
            }
        }

        return false;
    }

    private boolean tryBuildRobot(RobotType type, MapLocation location) throws GameActionException {
        if (rc.canBuildRobot(type, location)) {
            rc.buildRobot(type, location);
            return true;
        }

        return false;
    }
}
