package team050;

import team050.rpc.Channels;
import team050.rpc.Clans;
import team050.rpc.Clans.ClanMode;
import team050.rpc.CoopNav;
import team050.rpc.Liveness;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
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

    // Computation code
    public boolean           isComputing   = false;
    public MapLocation       computeTarget = null;

    public HQRobot(RobotController myRC) throws GameActionException {
        super(myRC);

        // Initialize clan waypoints to base HQ.
        MapLocation hq = rc.senseHQLocation();
        for (int clan = 0; clan < Channels.MAX_CLANS; clan++) {
            Clans.setWaypoint(clan, hq);
        }

        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();
        me = rc.getTeam();
        enemy = me.opponent();
        enemyHQ = rc.senseEnemyHQLocation();
        nextPastrSite = scoutNextPasture();

        CoopNav.requestComputation(rc.senseEnemyHQLocation(), GeneralNavigation.coarseness);
        CoopNav.requestComputation(nextPastrSite, GeneralNavigation.coarseness);
        CoopNav.requestComputation(new MapLocation(mapWidth / 2, mapHeight / 2),
                GeneralNavigation.coarseness);
        CoopNav.requestComputation(rc.getLocation(), GeneralNavigation.coarseness);

        // Be safe here
        Dijkstra.byteCodeThresh = 1500;
    }

    protected void getUpdates() throws GameActionException {
        // Check if we are free to take a new computing job
        if (!isComputing) {
            computeTarget = CoopNav.claimNextAvailableJob();

            // Check whether there is computation that needs to be done
            if (computeTarget == null) {
                return;
            }

            // Tell GeneralNavigation what the next job should be
            GeneralNavigation.prepareCompute(rc, computeTarget, RobotType.HQ, 39);

            // Set the computing flag
            isComputing = true;
        }
    }

    protected void updateInternals() throws GameActionException {
        if (Clock.getRoundNum() % 10 != 0) {
            return;
        }
        // Check for liveness & maintain liveness state table.
        for (int gid = 0; gid < Channels.MAX_ROBOTS; gid++) {
            int lastUpdatedRound = Liveness.getLastPostedRoundByGid(gid);
            if (lastUpdatedRound == 0) {
                continue;
            }
            if (lastUpdatedRound < (Clock.getRoundNum() - Liveness.LIVENESS_UPDATE_PERIOD - 1)) {
                int clan = gid / 10;
                Clans.setClanSize(clan, Clans.getClanSize(clan) - 1);
                Liveness.clearLiveness(gid);
            }
        }

        for (int clan = 0; clan < 4; clan++) {
            boolean hasPastr = false;
            boolean hasNT = false;
            for (int bot = 0; bot < 10; bot++) {
                int gid = 10 * clan + bot;
                if (Liveness.getLastPostedType(gid) == RobotType.PASTR)
                    hasPastr = true;
                if (Liveness.getLastPostedType(gid) == RobotType.NOISETOWER)
                    hasNT = true;
            }
            Clans.setClanPastrStatus(clan, hasPastr);
            Clans.setClanNTStatus(clan, hasNT);
        }

        // Manage clans.
        for (int i = 0; i < 4; i++) {
            switch (Clans.getClanMode(i)) {
                case IDLE:
                    manageIdleClan(i);
                    break;
                case RAIDER:
                    manageRaider(i);
                    break;
                case DEFENDER:
                    manageDefender(i);
                    break;
                default:
                    break;
            }
        }
    }

    private void manageDefender(int i) throws GameActionException {
        // pass
    }

    public void manageRaider(int clan) throws GameActionException {
        MapLocation target = Clans.getWaypoint(clan);

        // Update to the location that is nearest to where you were
        MapLocation[] pastrLocations = rc.sensePastrLocations(enemy);
        if (pastrLocations.length > 0) {
            MapLocation possible = pastrLocations[0];
            for (MapLocation p : pastrLocations) {
                CoopNav.requestComputation(target, GeneralNavigation.coarseness);
                if (p.distanceSquaredTo(target) < possible.distanceSquaredTo(target)) {
                    possible = p;
                }
            }
            target = possible;
        }
        else {
            MapLocation[] broadcast = rc.senseBroadcastingRobotLocations(enemy);
            if (Clock.getRoundNum() > 500 && broadcast.length > 0) {
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
                        target = rc.senseHQLocation();
                    }
                }
            }
        }

        if (target.distanceSquaredTo(enemyHQ) < 25) {
            target = nextPastrSite;
        }
        Clans.setWaypoint(clan, target);
    }

    /*
     * The big thing here is to instigate the evolution from IDLE to the end-type for this clan.
     * 
     * We first check what the eventual mode will be for this idle clan. We then check if the clan
     * is large enough, and if it is, we evolve the clan to its new mode and dispatch it.
     */
    public void manageIdleClan(int clan) throws GameActionException {
        ClanMode newMode = Clans.TARGET_CLAN_TYPES[clan];
        switch (newMode) {
            case BUILDER:
                if (Clans.getClanSize(clan) >= 2) {
                    Clans.setWaypoint(clan, nextPastrSite);
                    Clans.setClanMode(clan, ClanMode.BUILDER);
                }
                break;
            case RAIDER:
                // If we are at the target size minus 1, or we're the last clan and have two cowboy
                if (Clans.getClanSize(clan) >= Clans.TARGET_CLAN_SIZES[clan] - 1
                        || (clan == Clans.TARGET_CLAN_SIZES.length - 1 && Clans.getClanSize(clan) >= 2)) {
                    Clans.setClanMode(clan, ClanMode.RAIDER);
                }
                break;
            default:
                break;
        }
    }

    protected void doAction() throws GameActionException {
        boolean doSpawn = false;
        Direction toSpawn = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
        // Check if a robot is spawnable and spawn one if it is
        if (rc.senseRobotCount() < GameConstants.MAX_ROBOTS) {
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

    protected void sendUpdates() throws GameActionException {
        // This is a flag that tells us if the computation has finished
        if (Dijkstra.finished) {
            // We are no longer computing once this is done
            isComputing = false;

            // We are now finished, do not want this to accidentally run again
            Dijkstra.finished = false;

            // Tell the cowboys all the wonderful computing that we have done
            CoopNav.postJobResult(rc, computeTarget, Dijkstra.previous);
        }
    }

    protected void doCompute() throws GameActionException {
        // If we have a job prepared, then do computation
        if (isComputing) {
            GeneralNavigation.doCompute();
        }
    }

    public MapLocation scoutNextPasture() {
        cowGrowth = rc.senseCowGrowth();
        double bestGrowth = cowGrowth[0][0];
        MapLocation bestSite = new MapLocation(0, 0);

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
        return growth;
    }
}
