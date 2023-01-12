package camel_case.robot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public abstract class Unit extends Robot {
    private MapLocation currentTarget;

    private boolean isBugMoving;
    private int distanceBeforeBugMoving;
    private boolean huggingLeftWall;
    private MapLocation lastHuggedWall;

    public Unit(RobotController rc, RobotType type) {
        super(rc, type);
    }

    protected boolean tryMove(Direction direction, boolean allowCurrent) throws GameActionException {
        if (!rc.isMovementReady()) {
            return false;
        }

        MapLocation target = rc.getLocation().add(direction);
        if (!allowCurrent && rc.onTheMap(target) && rc.senseMapInfo(target).getCurrentDirection() != Direction.CENTER) {
            return false;
        }

        if (rc.canMove(direction)) {
            rc.move(direction);
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

            isBugMoving = false;
            lastHuggedWall = null;
        }

        for (Direction direction : adjacentDirections) {
            MapLocation location = rc.getLocation().add(direction);
            if (!rc.onTheMap(location)) {
                continue;
            }

            Direction current = rc.senseMapInfo(location).getCurrentDirection();
            if (current == Direction.CENTER) {
                continue;
            }

            if (location.add(current).distanceSquaredTo(target) >= rc.getLocation().distanceSquaredTo(target)) {
                continue;
            }

            if (tryMove(direction, true)) {
                isBugMoving = false;
                lastHuggedWall = null;
                return true;
            }
        }

        if (isBugMoving) {
            if (rc.getLocation().distanceSquaredTo(currentTarget) < distanceBeforeBugMoving) {
                if (tryMove(directionTowards(currentTarget), false)) {
                    isBugMoving = false;
                    lastHuggedWall = null;
                    return true;
                }
            }
        } else {
            if (tryMove(directionTowards(currentTarget), false)) {
                return true;
            } else {
                isBugMoving = true;
                distanceBeforeBugMoving = rc.getLocation().distanceSquaredTo(currentTarget);

                determineBugMoveDirection();
            }
        }

        return makeBugMove(true);
    }

    private void determineBugMoveDirection() {
        Direction forward = directionTowards(currentTarget);

        Direction left = forward.rotateLeft();
        int leftDistance = Integer.MAX_VALUE;

        Direction right = forward.rotateRight();
        int rightDistance = Integer.MAX_VALUE;

        for (int i = 0; i < 8; i++) {
            if (rc.canMove(left)) {
                leftDistance = rc.getLocation().add(left).distanceSquaredTo(currentTarget);
                break;
            }

            left = left.rotateLeft();
        }

        for (int i = 0; i < 8; i++) {
            if (rc.canMove(right)) {
                rightDistance = rc.getLocation().add(right).distanceSquaredTo(currentTarget);
                break;
            }

            right = right.rotateRight();
        }

        if (leftDistance < rightDistance) {
            huggingLeftWall = true;
            lastHuggedWall = rc.getLocation().add(right.rotateLeft());
        } else {
            huggingLeftWall = false;
            lastHuggedWall = rc.getLocation().add(left.rotateRight());
        }
    }

    private boolean makeBugMove(boolean firstCall) throws GameActionException {
        Direction currentDirection = directionTowards(lastHuggedWall);

        for (int i = 0; i < 8; i++) {
            if (huggingLeftWall) {
                currentDirection = currentDirection.rotateRight();
            } else {
                currentDirection = currentDirection.rotateLeft();
            }

            MapLocation newLocation = rc.getLocation().add(currentDirection);

            if (firstCall && !rc.onTheMap(newLocation)) {
                huggingLeftWall = !huggingLeftWall;
                return makeBugMove(false);
            }

            if (tryMove(currentDirection, false)) {
                return true;
            } else {
                lastHuggedWall = rc.getLocation().add(currentDirection);
            }
        }

        return false;
    }
}
