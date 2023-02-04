package camel_case_v30_final.robot;

import battlecode.common.Anchor;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.ResourceType;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.WellInfo;

public class Headquarters extends Robot {
    private int lastAnchorRound = 0;
    private int lastDangerRound = 0;

    public Headquarters(RobotController rc) {
        super(rc, RobotType.HEADQUARTERS);
    }

    @Override
    public void run() throws GameActionException {
        int turnIndex = sharedArray.getHeadquartersTurnIndex();

        if (rc.getRoundNum() == 1) {
            sharedArray.setMyHqLocation(turnIndex, rc.getLocation());
        }

        for (RobotInfo robot : rc.senseNearbyRobots(me.visionRadiusSquared, opponentTeam)) {
            if (robot.type == RobotType.LAUNCHER) {
                lastDangerRound = rc.getRoundNum();
                break;
            }
        }

        boolean buildingAnchor = rc.getNumAnchors(Anchor.STANDARD) == 0
            && rc.getRobotCount() > sharedArray.getHqCount() * 10
            && rc.getRoundNum() - lastAnchorRound > 100
            && rc.getRoundNum() - lastDangerRound > 300;

        if (buildingAnchor && tryBuildAnchor(Anchor.STANDARD)) {
            lastAnchorRound = rc.getRoundNum();
            buildingAnchor = false;
        }

        if (rc.getRoundNum() <= 1750) {
            int anchorMana = Anchor.STANDARD.manaCost;
            int launcherMana = RobotType.LAUNCHER.buildCostMana;

            while (true) {
                if (buildingAnchor && rc.getResourceAmount(ResourceType.MANA) - launcherMana < anchorMana) {
                    break;
                }

                if (!tryBuildRobot(RobotType.LAUNCHER)) {
                    break;
                }
            }
        }

        int anchorAdamantium = Anchor.STANDARD.adamantiumCost;
        int carrierAdamantium = RobotType.CARRIER.buildCostAdamantium;

        while (true) {
            if (buildingAnchor && rc.getResourceAmount(ResourceType.ADAMANTIUM) - carrierAdamantium < anchorAdamantium) {
                break;
            }

            if (!tryBuildRobot(RobotType.CARRIER)) {
                break;
            }
        }
    }

    private boolean tryBuildRobot(RobotType type) throws GameActionException {
        if (!rc.isActionReady() || !hasResources(type)) {
            return false;
        }

        MapLocation location;
        MapLocation center = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2);

        if (type != RobotType.CARRIER) {
            location = getBuildLocationNear(type, center);
        } else if (rc.getRoundNum() == 1) {
            location = getBuildLocationAwayFrom(type, center);
        } else {
            location = getBuildLocationNearWell(ResourceType.MANA);

            if (location == null) {
                location = getBuildLocationNearWell(ResourceType.ADAMANTIUM);
            }

            if (location == null) {
                location = getBuildLocationNear(type, center);
            }
        }

        if (location == null) {
            return false;
        }

        return tryBuildRobot(type, location);
    }

    private MapLocation getBuildLocationNear(RobotType type, MapLocation target) throws GameActionException {
        MapLocation bestLocation = null;
        int minDistance = Integer.MAX_VALUE;

        for (MapLocation location : rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), me.actionRadiusSquared)) {
            if (!rc.canBuildRobot(type, location)) {
                continue;
            }

            int distance = location.distanceSquaredTo(target);
            if (distance < minDistance) {
                bestLocation = location;
                minDistance = distance;
            }
        }

        return bestLocation;
    }

    private MapLocation getBuildLocationAwayFrom(RobotType type, MapLocation target) throws GameActionException {
        MapLocation bestLocation = null;
        int maxDistance = Integer.MIN_VALUE;

        for (MapLocation location : rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), me.actionRadiusSquared)) {
            if (!rc.canBuildRobot(type, location)) {
                continue;
            }

            int distance = location.distanceSquaredTo(target);
            if (distance > maxDistance) {
                bestLocation = location;
                maxDistance = distance;
            }
        }

        return bestLocation;
    }

    private MapLocation getBuildLocationNearWell(ResourceType resource) throws GameActionException {
        WellInfo[] wells = rc.senseNearbyWells(resource);
        if (wells.length == 0) {
            return null;
        }

        MapLocation bestLocation = null;
        int bestDistance = Integer.MAX_VALUE;

        for (MapLocation location : rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), me.actionRadiusSquared)) {
            if (!rc.canBuildRobot(RobotType.CARRIER, location)) {
                continue;
            }

            int distance = Integer.MAX_VALUE;
            for (WellInfo well : wells) {
                distance = Math.min(distance, well.getMapLocation().distanceSquaredTo(location));
            }

            if (distance < bestDistance) {
                bestLocation = location;
                bestDistance = distance;
            }
        }

        return bestLocation;
    }

    private boolean hasResources(RobotType type) {
        for (ResourceType resource : resourceTypes) {
            if (rc.getResourceAmount(resource) < type.getBuildCost(resource)) {
                return false;
            }
        }

        return true;
    }

    private boolean tryBuildRobot(RobotType type, MapLocation location) throws GameActionException {
        if (rc.canBuildRobot(type, location)) {
            rc.buildRobot(type, location);
            return true;
        }

        return false;
    }

    private boolean tryBuildAnchor(Anchor anchor) throws GameActionException {
        if (rc.canBuildAnchor(anchor)) {
            rc.buildAnchor(anchor);
            return true;
        }

        return false;
    }
}
