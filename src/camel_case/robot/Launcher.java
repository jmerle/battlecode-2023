package camel_case.robot;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Launcher extends Unit {
    public Launcher(RobotController rc) {
        super(rc, RobotType.LAUNCHER);
    }

    @Override
    public void run() throws GameActionException {
        lookForDangerTargets();

        RobotInfo attackTarget = getAttackTarget(me.actionRadiusSquared);
        if (attackTarget != null && tryAttack(attackTarget.location)) {
            return;
        }

        RobotInfo visibleTarget = getAttackTarget(me.visionRadiusSquared);
        if (visibleTarget != null && tryMoveToAndAttack(visibleTarget.location)) {
            return;
        }

        MapLocation dangerTarget = getClosestDangerTarget();
        rc.setIndicatorString(dangerTarget == null ? "nope" : "yes");
        if (tryMoveToAndAttack(getClosestDangerTarget())) {
            return;
        }

        tryWander();
    }
}
