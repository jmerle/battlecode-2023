package v01.robot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.ResourceType;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.Team;

public abstract class Robot {
    protected RobotController rc;

    protected RobotType me;

    protected Team myTeam;
    protected Team opponentTeam;

    protected int mapWidth;
    protected int mapHeight;

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

    public Robot(RobotController rc, RobotType type) {
        this.rc = rc;

        me = type;

        myTeam = rc.getTeam();
        opponentTeam = myTeam.opponent();

        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();
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
