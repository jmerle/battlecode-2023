package camel_case_v05.robot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.ResourceType;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import camel_case_v05.util.SharedArray;

public abstract class Robot {
    protected RobotController rc;

    protected RobotType me;

    protected Team myTeam;
    protected Team opponentTeam;

    protected Direction[] allDirections = Direction.values();
    protected Direction[] adjacentDirections = {
            Direction.NORTH,
            Direction.EAST,
            Direction.SOUTH,
            Direction.WEST,
            Direction.NORTHEAST,
            Direction.SOUTHEAST,
            Direction.SOUTHWEST,
            Direction.NORTHWEST
    };

    protected ResourceType[] resourceTypes = {
            ResourceType.ADAMANTIUM,
            ResourceType.MANA,
            ResourceType.ELIXIR
    };

    protected SharedArray sharedArray;

    public Robot(RobotController rc, RobotType type) {
        this.rc = rc;

        me = type;

        myTeam = rc.getTeam();
        opponentTeam = myTeam.opponent();

        sharedArray = new SharedArray(rc);
    }

    public abstract void run() throws GameActionException;

    protected void lookForDangerTargets() throws GameActionException {
        if (!sharedArray.canWrite()) {
            return;
        }

        MapLocation myLocation = rc.getLocation();
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();

        int opponentCount = 0;

        RobotInfo closestTarget = null;
        int minDistance = Integer.MAX_VALUE;

        for (RobotInfo robot : nearbyRobots) {
            if (robot.team != opponentTeam || robot.type == RobotType.HEADQUARTERS) {
                continue;
            }

            opponentCount++;

            int distance = myLocation.distanceSquaredTo(robot.location);
            if (distance < minDistance) {
                closestTarget = robot;
                minDistance = distance;
            }
        }

        if (closestTarget == null) {
            return;
        }

        int defenderCount = me == RobotType.LAUNCHER ? 1 : 0;
        for (RobotInfo robot : nearbyRobots) {
            if (robot.team == myTeam
                    && robot.type == RobotType.LAUNCHER
                    && robot.location.distanceSquaredTo(closestTarget.location) <= robot.type.actionRadiusSquared) {
                defenderCount++;
            }
        }

        if (opponentCount > defenderCount) {
            sharedArray.addDangerTarget(closestTarget.location, 10);
        }
    }

    protected MapLocation getClosestDangerTarget() throws GameActionException {
        MapLocation myLocation = rc.getLocation();

        MapLocation closestTarget = null;
        int minDistance = Integer.MAX_VALUE;

        for (int i = 0; i < SharedArray.MAX_DANGER_TARGETS; i++) {
            MapLocation target = sharedArray.getDangerTarget(i);
            if (target == null) {
                continue;
            }

            int distance = myLocation.distanceSquaredTo(target);
            if (distance < minDistance) {
                closestTarget = target;
                minDistance = distance;
            }
        }

        return closestTarget;
    }

    protected Direction directionTowards(MapLocation from, MapLocation to) {
        if (from.x < to.x && from.y < to.y) {
            return Direction.NORTHEAST;
        } else if (from.x < to.x && from.y > to.y) {
            return Direction.SOUTHEAST;
        } else if (from.x > to.x && from.y < to.y) {
            return Direction.NORTHWEST;
        } else if (from.x > to.x && from.y > to.y) {
            return Direction.SOUTHWEST;
        } else if (from.x < to.x) {
            return Direction.EAST;
        } else if (from.x > to.x) {
            return Direction.WEST;
        } else if (from.y < to.y) {
            return Direction.NORTH;
        } else if (from.y > to.y) {
            return Direction.SOUTH;
        } else {
            return Direction.CENTER;
        }
    }

    protected Direction directionTowards(MapLocation to) {
        return directionTowards(rc.getLocation(), to);
    }
}
