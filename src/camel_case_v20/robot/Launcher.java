package camel_case_v20.robot;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import camel_case_v20.util.Symmetry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class Launcher extends Unit {
    private int[][] RANGE_5_TO_16 = {
        {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}, {-2, -2}, {-2, 2}, {2, -2}, {2, 2},
        {-3, 0}, {0, -3}, {0, 3}, {3, 0}, {-3, -1}, {-3, 1}, {-1, -3}, {-1, 3}, {1, -3}, {1, 3}, {3, -1}, {3, 1},
        {-3, -2}, {-3, 2}, {-2, -3}, {-2, 3}, {2, -3}, {2, 3}, {3, -2}, {3, 2}, {-4, 0}, {0, -4}, {0, 4}, {4, 0}
    };

    private List<MapLocation> opponentHqs;
    private int opponentHqIndex;

    private MapLocation hqLocation;

    public Launcher(RobotController rc) {
        super(rc, RobotType.LAUNCHER);
    }

    @Override
    public void run() throws GameActionException {
        if (opponentHqs == null) {
            findOpponentHqs();
        } else {
            pruneOpponentHqs();
        }

        if (hqLocation == null) {
            for (RobotInfo robot : rc.senseNearbyRobots(me.visionRadiusSquared, myTeam)) {
                if (robot.type == RobotType.HEADQUARTERS) {
                    hqLocation = robot.location;
                    break;
                }
            }
        }

        act();
        act();

        tryBlindAttack();
    }

    private void act() throws GameActionException {
        RobotInfo attackTarget = getAttackTarget(me.actionRadiusSquared);
        if (attackTarget != null) {
            tryAttack(attackTarget.location);
            tryMoveToSafety();
            return;
        }

        RobotInfo visibleTarget = getAttackTarget(me.visionRadiusSquared);
        if (visibleTarget != null) {
            tryMoveTo(visibleTarget.location);
            return;
        }

        if (tryMoveToOpponentHq()) {
            return;
        }

        tryWander();
    }

    private void tryBlindAttack() throws GameActionException {
        if (!rc.isActionReady()) {
            return;
        }

        int distanceToHq = hqLocation.distanceSquaredTo(rc.getLocation());

        for (int[] dxdy : RANGE_5_TO_16) {
            MapLocation location = rc.getLocation().translate(dxdy[0], dxdy[1]);
            if (rc.senseCloud(location) && hqLocation.distanceSquaredTo(location) > distanceToHq && tryAttack(location)) {
                return;
            }
        }

        if (!rc.senseCloud(rc.getLocation())) {
            return;
        }

        for (int[] dxdy : RANGE_5_TO_16) {
            MapLocation location = rc.getLocation().translate(dxdy[0], dxdy[1]);
            if (hqLocation.distanceSquaredTo(location) > distanceToHq && tryAttack(location)) {
                return;
            }
        }
    }

    private void findOpponentHqs() throws GameActionException {
        // +----+----+
        // | Q1 | Q2 |
        // +----+----+
        // | Q3 | Q4 |
        // +----+----+
        boolean hqInQ1 = false;
        boolean hqInQ2 = false;
        boolean hqInQ3 = false;
        boolean hqInQ4 = false;

        int midX = rc.getMapWidth() / 2;
        int midY = rc.getMapHeight() / 2;

        int hqCount = sharedArray.getHqCount();
        List<MapLocation> myHqs = new ArrayList<>();

        for (int i = 0; i < hqCount; i++) {
            MapLocation location = sharedArray.getMyHqLocation(i);
            myHqs.add(location);

            if (location.x < midX && location.y < midY) {
                hqInQ1 = true;
            } else if (location.x >= midX && location.y < midY) {
                hqInQ2 = true;
            } else if (location.x < midX && location.y >= midY) {
                hqInQ3 = true;
            } else {
                hqInQ4 = true;
            }
        }

        opponentHqs = new ArrayList<>();

        if (!(hqInQ1 && hqInQ2 && hqInQ3 && hqInQ4)) {
            for (MapLocation hq : myHqs) {
                opponentHqs.add(Symmetry.ROTATIONAL.reflect(rc, hq));
            }
        }

        if (!(hqInQ1 && hqInQ3) && !(hqInQ2 && hqInQ4)) {
            for (MapLocation hq : myHqs) {
                opponentHqs.add(Symmetry.HORIZONTAL.reflect(rc, hq));
            }
        }

        if (!(hqInQ1 && hqInQ2) && !(hqInQ3 && hqInQ4)) {
            for (MapLocation hq : myHqs) {
                opponentHqs.add(Symmetry.VERTICAL.reflect(rc, hq));
            }
        }

        opponentHqs.sort(Comparator.comparingInt(hq -> rc.getLocation().distanceSquaredTo(hq)));
        opponentHqIndex = 0;
    }

    private void pruneOpponentHqs() throws GameActionException {
        if (opponentHqs.isEmpty()) {
            return;
        }

        Iterator<MapLocation> it = opponentHqs.iterator();
        while (it.hasNext()) {
            MapLocation location = it.next();
            if (!rc.canSenseRobotAtLocation(location)) {
                continue;
            }

            RobotInfo robot = rc.senseRobotAtLocation(location);
            if (robot.team != opponentTeam || robot.type != RobotType.HEADQUARTERS) {
                it.remove();
            }
        }

        if (!opponentHqs.isEmpty()) {
            opponentHqIndex %= opponentHqs.size();
        }
    }

    private boolean tryMoveToOpponentHq() throws GameActionException {
        if (opponentHqs.isEmpty()) {
            return false;
        }

        if (isStuck(opponentHqs.get(opponentHqIndex))) {
            opponentHqs.remove(opponentHqIndex);

            if (opponentHqs.isEmpty()) {
                return false;
            }

            opponentHqIndex %= opponentHqs.size();
        }

        if (rc.canSenseLocation(opponentHqs.get(opponentHqIndex))) {
            opponentHqIndex = (opponentHqIndex + 1) % opponentHqs.size();
        }

        tryMoveTo(opponentHqs.get(opponentHqIndex));
        return true;
    }
}
