package team050;

import team050.rpc.Channels;
import team050.rpc.Clans;
import team050.rpc.Clans.ClanMode;
import team050.rpc.Liveness;
import battlecode.common.Clock;
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

    public MapLocation nextPastrSite;

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
        // Update state of each robot every round.
        int curRound = Clock.getRoundNum();
        if (curRound >= 2) {
            System.out.println("Round " + curRound);
            System.out.println("\tGarbage collecting game state.");
            for (int i = 0; i < Channels.MAX_GAME_OBJS; i++) {
                // If robot didn't update last round, it died.
                if (!robotIsDead(curRound, i)) {
                    continue;
                }
                // Robot must be dead.
                // - Update the present table.
                // - Decrease its clan size.
                // - Depending on its type, clan might need to rebuild some structures.
                /*
                 * int clan = Clans.getMembershipByGid(i); Clans.setSize(clan, Clans.getSize(clan) -
                 * 1); switch (Clans.getClanMode(clan)) { case DEFENDER: case BUILDER: switch
                 * (Liveness.getLastPostedTypeByGid(i)) { case NOISETOWER:
                 * Clans.setClanNTStatus(clan, false); break; case PASTR:
                 * Clans.setClanPastrStatus(clan, false); break; } break; default: break; }
                 */
            }
        }

        // Manage clans.
        for (int i = 0; i < Clans.getNumClans(); i++) {
            switch (Clans.getClanMode(i)) {
                case IDLE:
                    manageIdleClan(i);
                    break;
                default:
                    break;
            }
        }

    }

    private boolean robotIsDead(int curRound, int gid) throws GameActionException {
        // Robots are considered dead if they did not update their state last round.
        return Liveness.getLastPostedRoundByGid(gid) < (curRound - 1);
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

    protected void sendUpdates() throws GameActionException {
        super.sendUpdates();
    }

    protected void doCompute() {
        // pass
    }

    public MapLocation scoutNextPasture(MapLocation hq) {
        // TODO: Do not select a site if we already have a PASTR there.
        double[][] cowGrowth = rc.senseCowGrowth();
        double bestGrowth = cowGrowth[0][0];
        MapLocation bestSite = new MapLocation(0, 0);

        int mapWidth = rc.getMapWidth();
        int mapHeight = rc.getMapHeight();
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                MapLocation loc = new MapLocation(x, y);
                double growth = cowGrowth[x][y];
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

}
