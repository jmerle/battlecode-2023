package camel_case.robot;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Carrier extends Unit {
    private boolean reachedTarget = false;

    public Carrier(RobotController rc) {
        super(rc, RobotType.CARRIER);
    }

    @Override
    public void run() throws GameActionException {
        if (reachedTarget) {
            return;
        }

        MapLocation target = new MapLocation(29, 0);

        MapLocation myLocation = rc.getLocation();
        if (myLocation.equals(target)) {
            System.out.println("Reached target in round " + rc.getRoundNum() + "!");
            reachedTarget = true;
            return;
        }

        tryMoveTo(target);
    }
}
