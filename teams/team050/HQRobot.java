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
    public int               mapWidth;
    public int               mapHeight;
    public final MapLocation enemyHQ;
    public final Team        me;
    public final Team        enemy;

    public HQRobot(RobotController myRC) throws GameActionException {
        super(myRC);

        // Initialize clan waypoints to base HQ.
        MapLocation hq = rc.senseHQLocation();
        for (int clan = 0; clan < Channels.MAX_CLANS; clan++) {
            Clans.setWaypoint(clan, hq);
        }

        me = rc.getTeam();
        enemy = me.opponent();
        enemyHQ = rc.senseEnemyHQLocation();
        nextPastrSite = scoutNextPasture();
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

        // Update to the location that is nearest to where you were
        MapLocation[] pastrLocations = rc.sensePastrLocations(enemy);
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
            MapLocation[] broadcast = rc.senseBroadcastingRobotLocations(enemy);
            if (broadcast.length > 0) {
                MapLocation possible = broadcast[0];
                for (MapLocation p : broadcast) {
                    if (p.distanceSquaredTo(target) < possible.distanceSquaredTo(target)
                            && (p.x != enemyHQ.x && p.y != enemyHQ.y)) {
                        possible = p;
                    }
                }
                target = possible;
            }
            else {
                // We are going to stay put then
                MapLocation[] myPastrs = rc.sensePastrLocations(me);
                if (myPastrs.length > 0) {
                    MapLocation best = myPastrs[0];
                    for (MapLocation p : myPastrs) {
                        if (p.distanceSquaredTo(enemyHQ) < best.distanceSquaredTo(enemyHQ)) {
                            best = p;
                        }
                    }
                    target = best;
                }
                else {
                    if (rc.senseTerrainTile(new MapLocation(mapWidth / 2, mapHeight / 2)) != TerrainTile.VOID) {
                        target = new MapLocation(mapWidth / 2, mapHeight / 2);
                    }
                    else {
                        target = enemyHQ;
                    }
                }
            }
        }
        Clans.setWaypoint(clan, target);
    }

    public void manageIdleClan(int clan) throws GameActionException {
        // Leave early since the other guy will catch up and we are waiting for large clans
        if (Clans.getSize(clan) >= Clans.DEFAULT_CLAN_SIZE - 1) {
            Clans.setClanMode(clan, ClanMode.RAIDER);
            // Clans.setWaypoint(clan, nextPastrSite);
            nextPastrSite = scoutNextPasture();
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
            // TODO: If we cannot spawn, then move the waypoint since we are being blocked by our
            // own robots
        }

        // Sense up to 15 because that is attack radius, but can splash to 21
        // Best attack is most kills, then most damage tiebreaker
        Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 15, enemy);

        MapLocation bestAttack = null;
        int bestKills = 0;
        double bestDamage = 0.0;

        for (int x = 0; x < nearbyEnemies.length; x++) {
            RobotInfo robotInfo = rc.senseRobotInfo(nearbyEnemies[x]);
            MapLocation robotLocation = robotInfo.location;

            // Do 50.0 damage to the target
            int kills = robotInfo.health <= 50.0 ? 1 : 0;
            double damage = Math.min(50.0, robotInfo.health);

            Robot[] splashAttack = rc.senseNearbyGameObjects(Robot.class, robotLocation, 2, enemy);
            for (Robot s : splashAttack) {
                // Does 25.0 damage in the splash radius
                double health = rc.senseRobotInfo(s).health;
                if (health <= 25.0) {
                    kills += 1;
                    damage += health;
                }
                else {
                    damage += 25.0;
                }
            }
            if (bestKills < kills || (bestKills == kills && bestDamage < damage)) {
                bestKills = kills;
                bestDamage = damage;
                bestAttack = robotLocation;
            }
        }

        if (bestAttack == null) {
            Robot[] splashEnemies = rc.senseNearbyGameObjects(Robot.class, 21, enemy);
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

    protected void doCompute() throws GameActionException {
        // pass
    }

    public MapLocation scoutNextPasture() {
        cowGrowth = rc.senseCowGrowth();
        double bestGrowth = cowGrowth[0][0];
        MapLocation bestSite = new MapLocation(0, 0);

        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();
        for (int x = 1; x < mapWidth - 1; x = x + 3) {
            for (int y = 1; y < mapHeight - 1; y = y + 3) {
                MapLocation loc = new MapLocation(x, y);
                double growth = scorePastrSite(x, y, loc);
                if (growth >= bestGrowth) {
                    bestSite = loc;
                    bestGrowth = growth;
                }
            }
        }
        return bestSite;
    }

    public double scorePastrSite(int x, int y, MapLocation loc) {
        // TODO: Check that this is actually reachable and not on a map with an unreachable bait
        double growth = cowGrowth[x - 1][y - 1] + cowGrowth[x - 1][y] + cowGrowth[x - 1][y + 1]
                + cowGrowth[x][y - 1] + cowGrowth[x][y] + cowGrowth[x][y + 1]
                + cowGrowth[x + 1][y - 1] + cowGrowth[x + 1][y] + cowGrowth[x + 1][y + 1];

        growth += .0001 * (loc.distanceSquaredTo(enemyHQ));
        // TODO: make a check for the lanes of how good clear direction is
        return growth;
    }
}
