package camel_case.robot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public abstract class Unit extends Robot {
    private MapLocation currentTarget;
    private boolean isWallFollowing;
    private int distanceBeforeWallFollowing;
    private boolean wallOnRight;
    private MapLocation lastFollowedWall;

    public Unit(RobotController rc, RobotType type) {
        super(rc, type);
    }

    protected boolean tryMove(Direction direction) throws GameActionException {
        if (rc.canMove(direction)) {
            rc.move(direction);
            rc.setIndicatorString("" + direction);
            return true;
        }

        return false;
    }

    protected boolean tryMoveTo(MapLocation target) throws GameActionException {
        if (!rc.isMovementReady()) {
            return false;
        }

        if (!target.equals(currentTarget)) {
            currentTarget = target;
            isWallFollowing = false;
        }

        int currentDistance = rc.getLocation().distanceSquaredTo(target);

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

        MapInfo mapInfo = rc.senseMapInfo(location);
        if (!mapInfo.isPassable() || mapInfo.getCurrentDirection() != Direction.CENTER) {
            return false;
        }

        RobotInfo robotInfo = rc.senseRobotAtLocation(location);
        return robotInfo == null || robotInfo.type != RobotType.HEADQUARTERS;
    }
}
