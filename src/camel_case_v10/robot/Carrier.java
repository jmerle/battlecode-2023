package camel_case_v10.robot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.ResourceType;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.WellInfo;

import java.util.HashMap;
import java.util.Map;

public class Carrier extends Unit {
    private Map<MapLocation, WellInfo> wells = new HashMap<>();

    private MapLocation hqLocation = null;
    private boolean isCollecting = true;

    public Carrier(RobotController rc) {
        super(rc, RobotType.CARRIER);
    }

    @Override
    public void run() throws GameActionException {
        if (hqLocation == null) {
            for (RobotInfo robot : rc.senseNearbyRobots(me.visionRadiusSquared, myTeam)) {
                if (robot.type == RobotType.HEADQUARTERS) {
                    hqLocation = robot.location;
                    break;
                }
            }
        }

        for (WellInfo well : rc.senseNearbyWells()) {
            MapLocation location = well.getMapLocation();
            if (wells.containsKey(location)) {
                if (wells.get(location) == null && !isReachable(location)) {
                    continue;
                }

                wells.put(location, well);
            } else {
                wells.put(location, isReachable(location) ? well : null);
            }
        }

        for (RobotInfo robot : rc.senseNearbyRobots(me.visionRadiusSquared, opponentTeam)) {
            if (robot.type == RobotType.LAUNCHER) {
                tryMoveToSafety();
                break;
            }
        }

        act();
        act();
    }

    private void act() throws GameActionException {
        ResourceType resourceTarget = rc.getID() % 2 == 0 ? ResourceType.ADAMANTIUM : ResourceType.MANA;
        if (rc.getRoundNum() < 50) {
            resourceTarget = ResourceType.MANA;
        }

        int cargo = getCargo();
        int cargoTarget = resourceTarget == ResourceType.ADAMANTIUM ? 25 : 30;

        if (isCollecting && cargo == cargoTarget) {
            isCollecting = false;
        }

        if (!isCollecting && cargo == 0) {
            isCollecting = true;
        }

        if (isCollecting) {
            WellInfo closestWell = null;
            int minDistance = Integer.MAX_VALUE;

            for (Map.Entry<MapLocation, WellInfo> entry : wells.entrySet()) {
                WellInfo well = entry.getValue();
                if (well == null || well.getResourceType() != resourceTarget) {
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
                tryCollectResource(closestWell.getMapLocation(), Math.min(closestWell.getRate(), cargoTarget - cargo));
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
    }

    private boolean isReachable(MapLocation location) throws GameActionException {
        MapLocation myLocation = rc.getLocation();

        while (!myLocation.equals(location)) {
            myLocation = myLocation.add(directionTowards(myLocation, location));

            if (!rc.onTheMap(myLocation) || !rc.canSenseLocation(myLocation)) {
                return false;
            }

            MapInfo mapInfo = rc.senseMapInfo(myLocation);
            if (!mapInfo.isPassable() || mapInfo.getCurrentDirection() != Direction.CENTER) {
                return false;
            }
        }

        return true;
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
