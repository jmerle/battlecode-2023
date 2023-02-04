package camel_case_v29.robot;

import battlecode.common.Anchor;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.ResourceType;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.WellInfo;
import camel_case_v29.RobotPlayer;
import camel_case_v29.util.MapLocationSet;
import camel_case_v29.util.RandomUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Carrier extends Unit {
    private Map<MapLocation, ResourceType> wells = new HashMap<>();
    private MapLocationSet sharedWells = new MapLocationSet();

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
        super.run();
        for (MapLocation location : sharedArray.getManaWellLocations(hqLocation)) {
            if (!sharedWells.contains(location)) {
                wells.put(location, ResourceType.MANA);
                sharedWells.add(location);
            }
        }

        if (!lookingForWell) {
            if (sharedArray.canWrite()) {
                for (Map.Entry<MapLocation, ResourceType> entry : wells.entrySet()) {
                    ResourceType resource = entry.getValue();
                    if (resource != ResourceType.MANA) {
                        continue;
                    }

                    MapLocation location = entry.getKey();
                    if (!sharedWells.contains(location)) {
                        sharedArray.setManaWellLocation(hqLocation, location);
                        sharedWells.add(location);
                    }
                }
            }

            if (islandLocations == null) {
                islandLocations = new MapLocation[rc.getIslandCount()];
                blockedIslands = new boolean[islandLocations.length];
            }

            for (int islandId : rc.senseNearbyIslands()) {
                if (islandLocations[islandId - 1] != null) {
                    continue;
                }

                islandLocations[islandId - 1] = rc.senseNearbyIslandLocations(islandId)[0];
            }
        }

        if (!lookingForWell) {
            int distanceToHq = rc.getLocation().distanceSquaredTo(hqLocation);
            for (RobotInfo robot : rc.senseNearbyRobots(me.visionRadiusSquared, myTeam)) {
                if (robot.type != RobotType.HEADQUARTERS) {
                    continue;
                }

                int newDistanceToHq = rc.getLocation().distanceSquaredTo(robot.location);
                if (newDistanceToHq + 50 <= distanceToHq) {
                    hqLocation = robot.location;
                    distanceToHq = newDistanceToHq;
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

        if (!lookingForWell) {
            for (WellInfo well : rc.senseNearbyWells(2, ResourceType.MANA)) {
                if (tryCollectResource(well.getMapLocation(), -1)) {
                    return;
                }
            }

            for (WellInfo well : rc.senseNearbyWells(2, ResourceType.ADAMANTIUM)) {
                if (tryCollectResource(well.getMapLocation(), -1)) {
                    return;
                }
            }
        }
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
                if (currentIslandId != -1) {
                    Team currentTeam = rc.senseTeamOccupyingIsland(currentIslandId);
                    if (currentTeam == Team.NEUTRAL) {
                        tryPlaceAnchor();
                        return;
                    } else if (currentTeam == opponentTeam) {
                        return;
                    }
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

                    if (rc.canSenseLocation(location) && rc.senseTeamOccupyingIsland(i + 1) == myTeam) {
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
                wells.put(well.getMapLocation(), well.getResourceType());
            }

            previousLocation = rc.getLocation();
        }

        if (isCollecting) {
            MapLocation bestWell = null;
            int minDistance = Integer.MAX_VALUE;

            for (Map.Entry<MapLocation, ResourceType> entry : wells.entrySet()) {
                if (entry.getValue() != resourceTarget) {
                    continue;
                }

                MapLocation location = entry.getKey();
                int distance = rc.getLocation().distanceSquaredTo(location);

                if (distance < minDistance) {
                    bestWell = location;
                    minDistance = distance;
                }
            }

            if (bestWell == null) {
                RobotPlayer.logBytecodeWarnings = false;

                if (!hasMarkedFrom.contains(rc.getLocation())) {
                    for (MapLocation location : rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), me.visionRadiusSquared)) {
                        hasSeen.add(location);
                    }

                    hasMarkedFrom.add(rc.getLocation());
                }

                lookingForWell = true;
                tryWander();
            } else if (rc.getLocation().isAdjacentTo(bestWell)) {
                Direction bestDirection = null;
                int minDistanceToHq = rc.getLocation().distanceSquaredTo(hqLocation);

                for (Direction direction : adjacentDirections) {
                    if (!rc.canMove(direction)) {
                        continue;
                    }

                    MapLocation newLocation = rc.getLocation().add(direction);
                    if (!newLocation.isAdjacentTo(bestWell)) {
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
                tryCollectResource(bestWell, -1);
            } else {
                lookingForWell = false;
                tryMoveTo(bestWell);
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
        for (Map.Entry<MapLocation, ResourceType> entry : wells.entrySet()) {
            if (entry.getValue() == ResourceType.ADAMANTIUM) {
                foundWell = true;
                beacons.add(entry.getKey());
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
