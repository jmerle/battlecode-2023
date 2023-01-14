package camel_case_v01.robot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.ResourceType;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.WellInfo;

import java.util.HashMap;
import java.util.Map;

public class Carrier extends Unit {
    private static final int CARGO_TARGET = 39;

    private Map<MapLocation, WellInfo> wells = new HashMap<>();

    private MapLocation hqLocation = null;
    private boolean isCollecting = true;

    public Carrier(RobotController rc) {
        super(rc, RobotType.CARRIER);
    }

    @Override
    public void run() throws GameActionException {
        if (hqLocation == null) {
            for (RobotInfo robot : rc.senseNearbyRobots(RobotType.HEADQUARTERS.actionRadiusSquared, myTeam)) {
                if (robot.type == RobotType.HEADQUARTERS) {
                    hqLocation = robot.location;
                    break;
                }
            }
        }

        ResourceType resourceTarget = rc.getID() % 2 == 0 ? ResourceType.ADAMANTIUM : ResourceType.MANA;
        act(true, resourceTarget);
    }

    private void act(boolean isFirstAction, ResourceType resourceTarget) throws GameActionException {
        int cargo = getCargo();

        if (isCollecting && cargo == CARGO_TARGET) {
            isCollecting = false;
        }

        if (!isCollecting && cargo == 0) {
            isCollecting = true;
        }

        for (Direction direction : allDirections) {
            MapLocation location = rc.adjacentLocation(direction);
            if (!rc.onTheMap(location)) {
                continue;
            }

            WellInfo well = rc.senseWell(location);
            if (well == null) {
                continue;
            }

            wells.put(location, well);
        }

        if (isCollecting) {
            WellInfo closestWell = null;
            int minDistance = Integer.MAX_VALUE;

            for (Map.Entry<MapLocation, WellInfo> entry : wells.entrySet()) {
                WellInfo well = entry.getValue();
                if (well.getResourceType() != resourceTarget) {
                    continue;
                }

                MapLocation location = well.getMapLocation();
                int distance = rc.getLocation().distanceSquaredTo(location);

                if (closestWell == null || distance < minDistance) {
                    closestWell = well;
                    minDistance = distance;
                }
            }

            if (closestWell == null) {
                tryWander();
            } else if (rc.getLocation().isAdjacentTo(closestWell.getMapLocation())) {
                tryCollectResource(closestWell.getMapLocation(), Math.min(closestWell.getRate(), CARGO_TARGET - cargo));
            } else {
                tryMoveTo(closestWell.getMapLocation());
            }
        } else {
            if (rc.getLocation().isAdjacentTo(hqLocation)) {
                for (ResourceType resource : resourceTypes) {
                    int amount = rc.getResourceAmount(resource);
                    if (amount == 0) {
                        continue;
                    }

                    if (tryTransferResource(hqLocation, resource, amount)) {
                        break;
                    }
                }
            } else {
                tryMoveTo(hqLocation);
            }
        }

        if (isFirstAction) {
            act(false, resourceTarget);
        }
    }

    private int getCargo() {
        return rc.getResourceAmount(ResourceType.ADAMANTIUM)
                + rc.getResourceAmount(ResourceType.MANA)
                + rc.getResourceAmount(ResourceType.ELIXIR);
    }

    private boolean tryCollectResource(MapLocation location, int amount) throws GameActionException {
        if (rc.canCollectResource(location, amount)) {
            rc.collectResource(location, amount);
            return true;
        }

        return false;
    }

    private boolean tryTransferResource(MapLocation location, ResourceType type, int amount) throws GameActionException {
        if (rc.canTransferResource(location, type, amount)) {
            rc.transferResource(location, type, amount);
            return true;
        }

        return false;
    }
}
