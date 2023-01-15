package camel_case.robot;

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
        int turnIndex = sharedArray.getHeadquartersTurnIndex();
        if (turnIndex == 0) {
            sharedArray.expireDangerTargets();
        }

        lookForDangerTargets();

        if (!rc.isActionReady()) {
            return;
        }

        int carriers = countFriendlies(RobotType.CARRIER);
        int launchers = countFriendlies(RobotType.LAUNCHER);
        int amplifiers = countFriendlies(RobotType.AMPLIFIER);

        RobotType type = RobotType.CARRIER;
        if (carriers > 5 && launchers > 5 && amplifiers == 0) {
            type = RobotType.AMPLIFIER;
        } else if (carriers > 0 && hasResources(RobotType.LAUNCHER)) {
            type = RobotType.LAUNCHER;
        }

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

    private int countFriendlies(RobotType type) throws GameActionException {
        int count = 0;

        for (RobotInfo robot : rc.senseNearbyRobots(me.visionRadiusSquared, myTeam)) {
            if (robot.type == type) {
                count++;
            }
        }

        return count;
    }

    private boolean tryBuildRobot(RobotType type, MapLocation location) throws GameActionException {
        if (rc.canBuildRobot(type, location)) {
            rc.buildRobot(type, location);
            return true;
        }

        return false;
    }
}
