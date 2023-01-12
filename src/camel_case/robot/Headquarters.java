package camel_case.robot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class Headquarters extends Robot {
    private boolean hasBuiltRobot = false;

    public Headquarters(RobotController rc) {
        super(rc, RobotType.HEADQUARTERS);
    }

    @Override
    public void run() throws GameActionException {
        if (myTeam == Team.B || hasBuiltRobot) {
            return;
        }

        RobotType type = RobotType.CARRIER;
        MapLocation location = rc.getLocation().add(Direction.NORTHWEST);

        if (rc.canBuildRobot(type, location)) {
            rc.buildRobot(type, location);
            hasBuiltRobot = true;
        }
    }
}
