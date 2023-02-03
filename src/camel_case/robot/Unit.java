package camel_case.robot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.ResourceType;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import camel_case.util.MapLocationSet;
import camel_case.util.RandomUtils;

public abstract class Unit extends Robot {
    private static final int[][] RANGE16 = {
        {0, 0}, {-1, 0}, {0, -1}, {0, 1}, {1, 0}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}, {-2, 0}, {0, -2}, {0, 2}, {2, 0},
        {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}, {-2, -2}, {-2, 2}, {2, -2}, {2, 2},
        {-3, 0}, {0, -3}, {0, 3}, {3, 0}, {-3, -1}, {-3, 1}, {-1, -3}, {-1, 3}, {1, -3}, {1, 3}, {3, -1}, {3, 1},
        {-3, -2}, {-3, 2}, {-2, -3}, {-2, 3}, {2, -3}, {2, 3}, {3, -2}, {3, 2}, {-4, 0}, {0, -4}, {0, 4}, {4, 0}
    };

    protected MapLocation hqLocation;

    private MapLocationSet dangerousLocations = new MapLocationSet();

    private MapLocation currentTarget;
    private boolean isWallFollowing;
    private int distanceBeforeWallFollowing;
    private boolean wallOnRight;
    private MapLocation lastFollowedWall;

    private int initialDistanceToTarget;
    private int turnsSpentMovingTowardsTarget;

    private MapLocation currentWanderTarget;

    public Unit(RobotController rc, RobotType type) {
        super(rc, type);
    }

    @Override
    public void run() throws GameActionException {
        if (hqLocation == null) {
            for (RobotInfo robot : rc.senseNearbyRobots(me.visionRadiusSquared, myTeam)) {
                if (robot.type == RobotType.HEADQUARTERS) {
                    hqLocation = robot.location;
                    break;
                }
            }

            if (hqLocation == null) {
                hqLocation = sharedArray.getMyHqLocation(0);
            }
        }

        if (rc.getMapWidth() * rc.getMapHeight() > 24 * 24) {
            outer:
            for (RobotInfo robot : rc.senseNearbyRobots(me.visionRadiusSquared, opponentTeam)) {
                if (robot.type != RobotType.HEADQUARTERS || dangerousLocations.contains(robot.location)) {
                    continue;
                }

                for (int i = sharedArray.getHqCount(); i-- > 0; ) {
                    MapLocation hq = sharedArray.getMyHqLocation(i);
                    if (hq.distanceSquaredTo(robot.location) <= RobotType.HEADQUARTERS.visionRadiusSquared) {
                        continue outer;
                    }
                }

                for (int[] dxdy : RANGE16) {
                    dangerousLocations.add(robot.location.translate(dxdy[0], dxdy[1]));
                }
            }
        }
    }

    protected RobotInfo getAttackTarget(int radius) throws GameActionException {
        RobotInfo bestTarget = null;
        int minHealth = Integer.MAX_VALUE;
        int maxPriority = Integer.MIN_VALUE;

        for (RobotInfo robot : rc.senseNearbyRobots(radius, opponentTeam)) {
            if (robot.type == RobotType.HEADQUARTERS) {
                continue;
            }

            int priority = robot.type.damage * 15
                + robot.getTotalAnchors() * 1000
                + robot.getResourceAmount(ResourceType.ELIXIR) * 5
                + robot.getResourceAmount(ResourceType.MANA) * 3
                + robot.getResourceAmount(ResourceType.ADAMANTIUM);

            if (bestTarget == null || priority > maxPriority || (priority == maxPriority && robot.health < minHealth)) {
                bestTarget = robot;
                minHealth = robot.health;
                maxPriority = priority;
            }
        }

        return bestTarget;
    }

    protected boolean tryMoveToSafety() throws GameActionException {
        if (!rc.isMovementReady()) {
            return false;
        }

        MapLocation myLocation = rc.getLocation();
        RobotInfo[] opponentRobots = rc.senseNearbyRobots(me.visionRadiusSquared, opponentTeam);

        Direction bestDirection = null;

        int maxDistance = 0;
        for (RobotInfo robot : opponentRobots) {
            if (robot.type.canAttack()) {
                maxDistance += myLocation.distanceSquaredTo(robot.location);
            }
        }

        for (Direction direction : adjacentDirections) {
            if (!rc.canMove(direction)) {
                continue;
            }

            MapLocation newLocation = rc.adjacentLocation(direction);

            int distance = 0;
            for (RobotInfo robot : opponentRobots) {
                if (robot.type.canAttack()) {
                    distance += newLocation.distanceSquaredTo(robot.location);
                }
            }

            if (distance > maxDistance) {
                bestDirection = direction;
                maxDistance = distance;
            }
        }

        if (bestDirection != null) {
            tryMove(bestDirection);
            return true;
        }

        return false;
    }

    protected MapLocation getNewWanderTarget() {
        return new MapLocation(RandomUtils.nextInt(rc.getMapWidth()), RandomUtils.nextInt(rc.getMapHeight()));
    }

    protected boolean tryWander() throws GameActionException {
        if (currentWanderTarget == null || rc.canSenseLocation(currentWanderTarget) || isStuck(currentWanderTarget)) {
            currentWanderTarget = getNewWanderTarget();
        }

        return tryMoveTo(currentWanderTarget);
    }

    protected boolean isStuck(MapLocation expectedTarget) {
        return expectedTarget.equals(currentTarget) && turnsSpentMovingTowardsTarget > initialDistanceToTarget * 5;
    }

    protected boolean tryMoveTo(MapLocation target) throws GameActionException {
        if (!rc.isMovementReady()) {
            return false;
        }

        int currentDistance = rc.getLocation().distanceSquaredTo(target);

        if (!target.equals(currentTarget)) {
            currentTarget = target;
            isWallFollowing = false;

            initialDistanceToTarget = currentDistance;
            turnsSpentMovingTowardsTarget = 0;
        }

        turnsSpentMovingTowardsTarget++;

        if (dangerousLocations.contains(rc.getLocation())) {
            Direction safeDirection = directionTowards(hqLocation);
            return tryMove(safeDirection)
                || tryMove(safeDirection.rotateLeft())
                || tryMove(safeDirection.rotateRight())
                || tryMove(safeDirection.rotateLeft().rotateLeft())
                || tryMove(safeDirection.rotateRight().rotateRight());
        }

        if (isWallFollowing && currentDistance < distanceBeforeWallFollowing) {
            isWallFollowing = false;
        }

        if (isWallFollowing && lastFollowedWall != null && rc.canSenseLocation(lastFollowedWall) && isPassable(lastFollowedWall)) {
            isWallFollowing = false;
        }

        if (!isWallFollowing) {
            for (Direction direction : adjacentDirections) {
                MapLocation location = rc.adjacentLocation(direction);
                if (!rc.onTheMap(location)) {
                    continue;
                }

                Direction current = rc.senseMapInfo(location).getCurrentDirection();
                Direction requiredDirection = directionTowards(location, target);
                if (current != requiredDirection
                    && current != requiredDirection.rotateLeft()
                    && current != requiredDirection.rotateRight()) {
                    continue;
                }

                if (location.add(current).distanceSquaredTo(target) >= currentDistance) {
                    continue;
                }

                if (tryMove(direction)) {
                    return true;
                }
            }

            Direction forward = directionTowards(target);
            if (isPassable(rc.adjacentLocation(forward))) {
                return tryMove(forward);
            } else {
                isWallFollowing = true;
                distanceBeforeWallFollowing = currentDistance;
                setInitialWallFollowingDirection();
            }
        }

        return followWall(true);
    }

    private void setInitialWallFollowingDirection() throws GameActionException {
        Direction forward = directionTowards(currentTarget);

        Direction left = forward.rotateLeft();
        int leftDistance = Integer.MAX_VALUE;

        Direction right = forward.rotateRight();
        int rightDistance = Integer.MAX_VALUE;

        for (int i = 0; i < 8; i++) {
            MapLocation location = rc.adjacentLocation(left);
            if (!rc.onTheMap(location)) {
                break;
            }

            if (isPassable(location)) {
                leftDistance = location.distanceSquaredTo(currentTarget);
                break;
            }

            left = left.rotateLeft();
        }

        for (int i = 0; i < 8; i++) {
            MapLocation location = rc.adjacentLocation(right);
            if (!rc.onTheMap(location)) {
                break;
            }

            if (isPassable(location)) {
                rightDistance = location.distanceSquaredTo(currentTarget);
                break;
            }

            right = right.rotateRight();
        }

        if (leftDistance < rightDistance) {
            wallOnRight = true;
            lastFollowedWall = rc.adjacentLocation(left.rotateRight());
        } else {
            wallOnRight = false;
            lastFollowedWall = rc.adjacentLocation(right.rotateLeft());
        }
    }

    private boolean followWall(boolean canRotate) throws GameActionException {
        Direction direction = directionTowards(lastFollowedWall);

        for (int i = 0; i < 8; i++) {
            direction = wallOnRight ? direction.rotateLeft() : direction.rotateRight();
            MapLocation location = rc.adjacentLocation(direction);

            if (canRotate && !rc.onTheMap(location)) {
                wallOnRight = !wallOnRight;
                return followWall(false);
            }

            if (isPassable(location) && tryMove(direction)) {
                return true;
            }

            lastFollowedWall = location;
        }

        return false;
    }

    private boolean isPassable(MapLocation location) throws GameActionException {
        if (!rc.onTheMap(location)) {
            return false;
        }

        if (rc.senseRobotAtLocation(location) != null) {
            return false;
        }

        MapInfo mapInfo = rc.senseMapInfo(location);
        if (!mapInfo.isPassable() || mapInfo.getCurrentDirection() != Direction.CENTER) {
            return false;
        }

        return !dangerousLocations.contains(location);
    }

    protected boolean tryMove(Direction direction) throws GameActionException {
        if (rc.canMove(direction)) {
            rc.move(direction);
            return true;
        }

        return false;
    }

    protected boolean tryAttack(MapLocation location) throws GameActionException {
        if (rc.canAttack(location)) {
            rc.attack(location);
            return true;
        }

        return false;
    }
}
