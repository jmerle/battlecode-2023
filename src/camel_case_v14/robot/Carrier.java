package camel_case_v14.robot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.ResourceType;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.WellInfo;
import camel_case_v14.RobotPlayer;
import camel_case_v14.util.MapLocationSet;

import java.util.HashMap;
import java.util.Map;

public class Carrier extends Unit {
    private Map<MapLocation, WellInfo> wells = new HashMap<>();

    private MapLocation hqLocation = null;
    private boolean isCollecting = true;

    private MapLocationSet hasSeen = new MapLocationSet();
    private MapLocationSet hasMarkedFrom = new MapLocationSet();

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
        ResourceType resourceTarget = ResourceType.MANA;

        int cargo = getCargo();
        int cargoTarget = 30;

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
                if (!hasMarkedFrom.contains(rc.getLocation())) {
                    RobotPlayer.logBytecodeWarnings = false;

                    for (MapLocation location : rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), me.visionRadiusSquared)) {
                        hasSeen.add(location);
                    }

                    hasMarkedFrom.add(rc.getLocation());
                }

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

    @Override
    protected boolean isWanderTargetValid(MapLocation target) {
        if (hasSeen.contains(target)) {
            return false;
        }

        boolean knownAdamantium = false;
        for (Map.Entry<MapLocation, WellInfo> entry : wells.entrySet()) {
            WellInfo well = entry.getValue();
            if (well == null || well.getResourceType() != ResourceType.ADAMANTIUM) {
                continue;
            }

            knownAdamantium = true;

            MapLocation location = entry.getKey();
            if (location.distanceSquaredTo(target) <= GameConstants.MAX_DISTANCE_BETWEEN_WELLS) {
                return true;
            }
        }

        if (knownAdamantium) {
            return false;
        }

        return hqLocation.distanceSquaredTo(target) <= GameConstants.MIN_NEAREST_AD_DISTANCE;
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
