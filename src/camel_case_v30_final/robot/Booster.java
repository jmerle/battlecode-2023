package camel_case_v30_final.robot;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Booster extends Unit {
    public Booster(RobotController rc) {
        super(rc, RobotType.BOOSTER);
    }

    @Override
    public void run() throws GameActionException {
        super.run();

        tryWander();
    }
}
