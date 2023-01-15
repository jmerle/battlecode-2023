package camel_case_v01.robot;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Destabilizer extends Unit {
    public Destabilizer(RobotController rc) {
        super(rc, RobotType.DESTABILIZER);
    }

    @Override
    public void run() throws GameActionException {
        tryWander();
    }
}
