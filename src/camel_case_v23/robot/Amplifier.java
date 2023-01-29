package camel_case_v23.robot;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Amplifier extends Unit {
    public Amplifier(RobotController rc) {
        super(rc, RobotType.AMPLIFIER);
    }

    @Override
    public void run() throws GameActionException {
        tryWander();
    }
}
