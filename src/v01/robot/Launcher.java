package v01.robot;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Launcher extends Unit {
    public Launcher(RobotController rc) {
        super(rc, RobotType.LAUNCHER);
    }

    @Override
    public void run() throws GameActionException {
        RobotInfo attackTarget = getAttackTarget(me.actionRadiusSquared);
        if (attackTarget != null && tryAttack(attackTarget.location)) {
            return;
        }

        RobotInfo visibleTarget = getAttackTarget(me.visionRadiusSquared);
        if (visibleTarget != null && tryMoveToAndAttack(visibleTarget.location)) {
            return;
        }

        tryWander();
    }
}
