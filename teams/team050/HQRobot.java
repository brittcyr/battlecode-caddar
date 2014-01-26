package team050;

import team050.rpc.Channels;
import team050.rpc.Clans;
import team050.rpc.Clans.ClanMode;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;
import battlecode.common.TerrainTile;

public class HQRobot extends BaseRobot {

    public MapLocation       nextPastrSite;
    public static double[][] cowGrowth;

    public HQRobot(RobotController myRC) throws GameActionException {
        super(myRC);

        // Initialize clan waypoints to base HQ.
        MapLocation hq = rc.senseHQLocation();
        for (int clan = 0; clan < Channels.MAX_CLANS; clan++) {
            Clans.setWaypoint(clan, hq);
        }

        nextPastrSite = scoutNextPasture(hq);
    }

    protected void getUpdates() {
        // pass
    }

    protected void updateInternals() throws GameActionException {
        // Manage clans.
        for (int i = 0; i < Clans.getNumClans(); i++) {
            switch (Clans.getClanMode(i)) {
                case IDLE:
                    manageIdleClan(i);
                    break;
                case RAIDER:
                    manageRaider(i);
                default:
                    break;
            }
        }
    }

    public void manageRaider(int clan) throws GameActionException {
        MapLocation target = Clans.getWaypoint(clan);

        // TODO: not have to check this every turn

        // Update to the location that is nearest to where you were
        MapLocation[] pastrLocations = rc.sensePastrLocations(rc.getTeam().opponent());
        if (pastrLocations.length > 0) {
            MapLocation possible = pastrLocations[0];
            for (MapLocation p : pastrLocations) {
                if (p.distanceSquaredTo(target) < possible.distanceSquaredTo(target)) {
                    possible = p;
                }
            }
            target = possible;
        }
        else {
            MapLocation[] broadcast = rc.senseBroadcastingRobotLocations();
            if (broadcast.length > 0) {
                MapLocation possible = broadcast[0];
                for (MapLocation p : broadcast) {
                    if (p.distanceSquaredTo(target) < possible.distanceSquaredTo(target)
                            && (p.x != rc.senseEnemyHQLocation().x && p.y != rc
                                    .senseEnemyHQLocation().y)) {
                        possible = p;
                    }
                }
                target = possible;
            }
            else {
                // We are going to stay put then
            }
        }
    }

    public void manageIdleClan(int clan) throws GameActionException {
        if (Clans.getSize(clan) >= Clans.DEFAULT_CLAN_SIZE) {
            Clans.setClanMode(clan, ClanMode.BUILDER);
            Clans.setWaypoint(clan, nextPastrSite);
            nextPastrSite = scoutNextPasture(rc.senseHQLocation());
        }
    }

    protected void doAction() throws GameActionException {
        boolean doSpawn = false;
        Direction toSpawn = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
        // Check if a robot is spawnable and spawn one if it is
        if (rc.isActive() && rc.senseRobotCount() < GameConstants.MAX_ROBOTS) {
            for (int rot = 0; rot < 8; rot++) {
                MapLocation test = rc.getLocation().add(toSpawn);
                if (rc.senseTerrainTile(test) != TerrainTile.VOID
                        && rc.senseTerrainTile(test) != TerrainTile.OFF_MAP
                        && rc.senseObjectAtLocation(test) == null) {
                    doSpawn = true;
                    break;
                }
                toSpawn = toSpawn.rotateLeft();
            }
        }

        // Sense up to 15 because that is attack radius, but can splash to 21
        // Best attack is most kills, then most damage tiebreaker
        Team opponent = rc.getTeam().opponent();
        Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 15, opponent);

        MapLocation bestAttack = null;
        int bestKills = 0;
        double bestDamage = 0.0;

        for (int x = 0; x < nearbyEnemies.length; x++) {
            RobotInfo robotInfo = rc.senseRobotInfo(nearbyEnemies[x]);
            MapLocation robotLocation = robotInfo.location;

            // Do 50.0 damage to the target
            int kills = robotInfo.health <= 50.0 ? 1 : 0;
            double damage = Math.min(50.0, robotInfo.health);

            Robot[] splashAttack = rc.senseNearbyGameObjects(Robot.class, robotLocation, 2,
                    opponent);
            for (Robot s : splashAttack) {
                // Does 25.0 damage in the splash radius
                kills += rc.senseRobotInfo(s).health <= 25.0 ? 1 : 0;
                damage += Math.min(25.0, rc.senseRobotInfo(s).health);
            }
            if (bestKills < kills || (bestKills == kills && bestDamage < damage)) {
                bestKills = kills;
                bestDamage = damage;
                bestAttack = robotLocation;
            }
        }

        if (bestAttack == null) {
            Robot[] splashEnemies = rc.senseNearbyGameObjects(Robot.class, 21, opponent);
            if (splashEnemies.length != 0) {
                // Then we have an enemy outside our attack but not splash radius
                // just attack the first one that we can
                Robot targetEnemy = splashEnemies[0];
                RobotInfo info = rc.senseRobotInfo(targetEnemy);
                MapLocation robotLocation = info.location;
                Direction toMe = robotLocation.directionTo(rc.getLocation());
                bestAttack = robotLocation.add(toMe);
            }
        }

        if (bestAttack != null) {
            rc.attackSquare(bestAttack);
            return;
        }

        if (doSpawn) {
            rc.spawn(toSpawn);
        }
    }

    protected void sendUpdates() {
        // pass
    }

    protected void doCompute() {
        // pass
    }

    public MapLocation scoutNextPasture(MapLocation hq) {
        cowGrowth = rc.senseCowGrowth();
        double bestGrowth = cowGrowth[0][0];
        MapLocation bestSite = new MapLocation(0, 0);

        int mapWidth = rc.getMapWidth();
        int mapHeight = rc.getMapHeight();
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                MapLocation loc = new MapLocation(x, y);
                double growth = scorePastrSite(x, y);
                if (growth >= bestGrowth) {
                    if ((growth == bestGrowth)
                            && (hq.distanceSquaredTo(loc) > hq.distanceSquaredTo(bestSite))) {
                        continue;  // Skip far symmetric case.
                    }
                    else {
                        bestSite = loc;
                        bestGrowth = growth;
                    }
                }
            }
        }
        return bestSite;
    }

    public double scorePastrSite(int x, int y) {
        // TODO: Check that this is actually reachable and not on a map with an unreachable bait

        if (x == 0 || y == 0 || x == rc.getMapWidth() - 1 || y == rc.getMapHeight() - 1) {
            return 0.0;
        }
        double growth = cowGrowth[x - 1][y - 1] + cowGrowth[x - 1][y] + cowGrowth[x - 1][y + 1]
                + cowGrowth[x][y - 1] + cowGrowth[x][y] + cowGrowth[x][y + 1]
                + cowGrowth[x + 1][y - 1] + cowGrowth[x + 1][y] + cowGrowth[x + 1][y + 1];

        growth += .0001 * (new MapLocation(x, y).distanceSquaredTo(rc.senseEnemyHQLocation()));
        // TODO: make a check for the lanes of how good clear direction is
        return growth;
    }

}
