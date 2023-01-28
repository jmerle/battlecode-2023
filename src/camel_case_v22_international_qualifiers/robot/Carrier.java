package camel_case_v22_international_qualifiers.robot;

import battlecode.common.Anchor;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.ResourceType;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.WellInfo;
import camel_case_v22_international_qualifiers.RobotPlayer;
import camel_case_v22_international_qualifiers.util.MapLocationSet;
import camel_case_v22_international_qualifiers.util.RandomUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Carrier extends Unit {
    private Map<MapLocation, WellInfo> wells = new HashMap<>();

    private MapLocation hqLocation = null;
    private boolean isCollecting = true;

    private MapLocationSet hasSeen = new MapLocationSet();
    private MapLocationSet hasMarkedFrom = new MapLocationSet();

    private MapLocation previousLocation = null;

    private boolean lookingForWell = true;

    private MapLocation[] islandLocations = null;
    private boolean[] blockedIslands = null;

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

        if (!lookingForWell) {
            if (islandLocations == null) {
                islandLocations = new MapLocation[rc.getIslandCount()];
                blockedIslands = new boolean[islandLocations.length];
            }

            for (int islandId : rc.senseNearbyIslands()) {
                if (islandLocations[islandId - 1] != null) {
                    continue;
                }

                MapLocation location = rc.senseNearbyIslandLocations(islandId)[0];
                if (isReachable(location)) {
                    islandLocations[islandId - 1] = location;
                }
            }
        }

        if (!lookingForWell) {
            int distanceToHq = rc.getLocation().distanceSquaredTo(hqLocation);
            for (RobotInfo robot : rc.senseNearbyRobots(me.visionRadiusSquared, myTeam)) {
                if (robot.type != RobotType.HEADQUARTERS) {
                    continue;
                }

                int newDistanceToHq = rc.getLocation().distanceSquaredTo(robot.location);
                if (newDistanceToHq + 50 > distanceToHq) {
                    continue;
                }

                if (isReachable(robot.location)) {
                    hqLocation = robot.location;
                }
            }
        }

        for (RobotInfo robot : rc.senseNearbyRobots(me.visionRadiusSquared, opponentTeam)) {
            if (robot.type == RobotType.LAUNCHER) {
                if (rc.getHealth() < me.getMaxHealth()) {
                    isCollecting = false;
                }

                tryMoveToSafety();
                break;
            }
        }

        act();
        act();
    }

    private void act() throws GameActionException {
        ResourceType resourceTarget = ResourceType.MANA;

        int cargo = rc.getWeight();
        int cargoTarget = 30;

        if (!lookingForWell) {
            if (cargo == 0 && rc.getLocation().isAdjacentTo(hqLocation)) {
                RobotInfo hq = rc.senseRobotAtLocation(hqLocation);
                if (hq.getNumAnchors(Anchor.STANDARD) > 0) {
                    tryTakeAnchor(hqLocation, Anchor.STANDARD);
                    return;
                }
            }

            if (rc.getNumAnchors(Anchor.STANDARD) > 0) {
                int currentIslandId = rc.senseIsland(rc.getLocation());
                if (currentIslandId != -1 && rc.senseTeamOccupyingIsland(currentIslandId) == Team.NEUTRAL) {
                    tryPlaceAnchor();
                    return;
                }

                MapLocation closestIsland = null;
                int minDistance = Integer.MAX_VALUE;

                for (int i = 0; i < islandLocations.length; i++) {
                    if (blockedIslands[i]) {
                        continue;
                    }

                    MapLocation location = islandLocations[i];
                    if (location == null) {
                        continue;
                    }

                    if (rc.canSenseLocation(location) && rc.senseTeamOccupyingIsland(i + 1) != Team.NEUTRAL) {
                        blockedIslands[i] = true;
                        continue;
                    }

                    int distance = rc.getLocation().distanceSquaredTo(location);
                    if (distance < minDistance) {
                        closestIsland = location;
                        minDistance = distance;
                    }
                }

                if (closestIsland != null) {
                    tryMoveTo(closestIsland);
                } else {
                    tryWander();
                }
                return;
            }
        }

        if (isCollecting && cargo == cargoTarget) {
            isCollecting = false;
        }

        if (!isCollecting && cargo == 0) {
            isCollecting = true;
        }

        if (!rc.getLocation().equals(previousLocation)) {
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

            previousLocation = rc.getLocation();
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
                RobotPlayer.logBytecodeWarnings = false;

                if (!hasMarkedFrom.contains(rc.getLocation())) {
                    for (MapLocation location : rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), me.visionRadiusSquared)) {
                        hasSeen.add(location);
                    }

                    hasMarkedFrom.add(rc.getLocation());
                }

                lookingForWell = true;
                tryWander();
            } else if (rc.getLocation().isAdjacentTo(closestWell.getMapLocation())) {
                Direction bestDirection = null;
                int minDistanceToHq = rc.getLocation().distanceSquaredTo(hqLocation);

                for (Direction direction : adjacentDirections) {
                    if (!rc.canMove(direction)) {
                        continue;
                    }

                    MapLocation newLocation = rc.getLocation().add(direction);
                    if (!newLocation.isAdjacentTo(closestWell.getMapLocation())) {
                        continue;
                    }

                    if (rc.senseMapInfo(newLocation).getCurrentDirection() != Direction.CENTER) {
                        continue;
                    }

                    int newDistance = newLocation.distanceSquaredTo(hqLocation);
                    if (newDistance <= minDistanceToHq) {
                        bestDirection = direction;
                        minDistanceToHq = newDistance;
                    }
                }

                if (bestDirection != null) {
                    tryMove(bestDirection);
                }

                lookingForWell = false;
                tryCollectResource(closestWell.getMapLocation(), Math.min(closestWell.getRate(), cargoTarget - cargo));
            } else {
                lookingForWell = false;
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
    protected MapLocation getNewWanderTarget() {
        if (rc.getNumAnchors(Anchor.STANDARD) > 0) {
            return super.getNewWanderTarget();
        }

        List<MapLocation> beacons = new ArrayList<>();

        boolean foundWell = false;
        for (Map.Entry<MapLocation, WellInfo> entry : wells.entrySet()) {
            WellInfo well = entry.getValue();
            if (well == null) {
                foundWell = true;
                continue;
            }

            if (well.getResourceType() == ResourceType.ADAMANTIUM) {
                foundWell = true;
                beacons.add(well.getMapLocation());
            }
        }

        if (beacons.isEmpty() && !foundWell) {
            beacons.add(hqLocation);
        }

        if (beacons.isEmpty()) {
            MapLocation target = null;
            while (target == null) {
                MapLocation possibleTarget = super.getNewWanderTarget();
                if (!rc.canSenseLocation(possibleTarget) && !hasSeen.contains(possibleTarget)) {
                    target = possibleTarget;
                }
            }

            return target;
        }

        MapLocation target = null;
        while (target == null) {
            MapLocation beacon = beacons.get(RandomUtils.nextInt(beacons.size()));
            MapLocation possibleTarget = beacon.translate(RandomUtils.nextInt(-10, 11), RandomUtils.nextInt(-10, 11));

            if (rc.onTheMap(possibleTarget)
                && !rc.canSenseLocation(possibleTarget)
                && possibleTarget.distanceSquaredTo(beacon) <= 100
                && !hasSeen.contains(possibleTarget)) {
                target = possibleTarget;
            }
        }

        return target;
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

    private boolean tryTakeAnchor(MapLocation location, Anchor anchor) throws GameActionException {
        if (rc.canTakeAnchor(location, anchor)) {
            rc.takeAnchor(location, anchor);
            return true;
        }

        return false;
    }

    private boolean tryPlaceAnchor() throws GameActionException {
        if (rc.canPlaceAnchor()) {
            rc.placeAnchor();
            return true;
        }

        return false;
    }
}
