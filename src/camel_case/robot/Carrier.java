package camel_case.robot;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Carrier extends Unit {
    private MapLocation target;

    public Carrier(RobotController rc) {
        super(rc, RobotType.CARRIER);
    }

    @Override
    public void run() throws GameActionException {
        if (rc.getLocation().equals(target)) {
            return;
        }

        if (target == null) {
            target = new MapLocation(rc.getLocation().x, mapHeight - 1);
        }

        tryMoveTo(target);
    }
}
