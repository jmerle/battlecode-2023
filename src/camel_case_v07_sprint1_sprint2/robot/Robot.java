package camel_case_v07_sprint1_sprint2.robot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.ResourceType;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.Team;
import camel_case_v07_sprint1_sprint2.util.SharedArray;

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
