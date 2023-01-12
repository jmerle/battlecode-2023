package camel_case.robot;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.Team;

public abstract class Robot {
    protected RobotController rc;

    protected RobotType me;

    protected Team myTeam;
    protected Team opponentTeam;

    public Robot(RobotController rc, RobotType type) {
        this.rc = rc;

        me = type;

        myTeam = rc.getTeam();
        opponentTeam = myTeam.opponent();
    }

    public abstract void run() throws GameActionException;
}
