package camel_case_v10.robot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.ResourceType;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import camel_case_v10.util.RandomUtils;

public abstract class Unit extends Robot {
    private MapLocation currentTarget;
    private boolean isWallFollowing;
    private int distanceBeforeWallFollowing;
    private boolean wallOnRight;
    private MapLocation lastFollowedWall;

    private int initialDistanceToTarget;
    private int turnsSpentMovingTowardsTarget;

    private Direction[] wanderQuadrants;
    private int wanderQuadrantIndex;
    private MapLocation currentWanderTarget;

    public Unit(RobotController rc, RobotType type) {
        super(rc, type);
    }

    protected RobotInfo getAttackTarget(int radius) throws GameActionException {
        RobotInfo bestTarget = null;
        int minHealth = Integer.MAX_VALUE;
        int maxPriority = Integer.MIN_VALUE;

        for (RobotInfo robot : rc.senseNearbyRobots(radius, opponentTeam)) {
            if (robot.type == RobotType.HEADQUARTERS) {
                continue;
            }

            int priority = robot.getTotalAnchors() * 1000 + robot.type.damage;
            if (robot.type == RobotType.CARRIER) {
                int cargo = robot.getResourceAmount(ResourceType.ADAMANTIUM)
                    + robot.getResourceAmount(ResourceType.MANA)
                    + robot.getResourceAmount(ResourceType.ELIXIR);

                priority += cargo / 5;
            }

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
            MapInfo mapInfo = rc.senseMapInfo(newLocation);
            if (mapInfo.hasCloud() || mapInfo.getCurrentDirection() != Direction.CENTER) {
                continue;
            }

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

    protected boolean tryWander() throws GameActionException {
        if (wanderQuadrants == null) {
            wanderQuadrants = RandomUtils.shuffle(new Direction[]{
                Direction.NORTHEAST,
                Direction.NORTHWEST,
                Direction.SOUTHEAST,
                Direction.SOUTHWEST
            });

            wanderQuadrantIndex = -1;
        }

        if (currentWanderTarget == null || rc.canSenseLocation(currentWanderTarget) || isStuck(currentWanderTarget)) {
            wanderQuadrantIndex = (wanderQuadrantIndex + 1) % 4;

            int minX = -1;
            int maxX = -1;
            int minY = -1;
            int maxY = -1;

            int mapWidth = rc.getMapWidth();
            int mapHeight = rc.getMapHeight();

            switch (wanderQuadrants[wanderQuadrantIndex]) {
                case NORTHEAST:
                    minX = mapWidth / 2;
                    maxX = mapWidth;
                    minY = mapHeight / 2;
                    maxY = mapHeight;
                    break;
                case NORTHWEST:
                    minX = 0;
                    maxX = mapWidth / 2;
                    minY = mapHeight / 2;
                    maxY = mapHeight;
                    break;
                case SOUTHEAST:
                    minX = mapWidth / 2;
                    maxX = mapWidth;
                    minY = 0;
                    maxY = mapHeight / 2;
                    break;
                case SOUTHWEST:
                    minX = 0;
                    maxX = mapWidth / 2;
                    minY = 0;
                    maxY = mapHeight / 2;
                    break;
            }

            currentWanderTarget = new MapLocation(RandomUtils.nextInt(minX, maxX), RandomUtils.nextInt(minY, maxY));
            return tryWander();
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

        if (isWallFollowing && currentDistance < distanceBeforeWallFollowing) {
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

        RobotInfo robotInfo = rc.senseRobotAtLocation(location);
        return robotInfo == null || robotInfo.type != RobotType.HEADQUARTERS;
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
