package team050;

import team050.rpc.Channels;
import team050.rpc.Clans;
import team050.rpc.Clans.ClanMode;
import team050.rpc.Liveness;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.TerrainTile;

public class NoiseTowerRobot extends BaseRobot {

    public static Direction[] directions = { Direction.NORTH, Direction.NORTH_EAST, Direction.EAST,
            Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST,
            Direction.NORTH_WEST        };
    public final Team         me;
    public final Team         enemy;
    public MapLocation        myPastr;
    public MapLocation        enemyPastr;
    public MapLocation        target;
    public MapLocation[]      myPastrs;
    public MapLocation[]      enemyPastrs;
    public Direction          dir;
    public int                dist;
    public double[][]         cowGrowth;
    public int[]              distInDirs;
    public final MapLocation  myLoc;
    public int                gid        = -1;
    public int                clan       = -1;

    public NoiseTowerRobot(RobotController myRC) throws GameActionException {
        super(myRC);
        me = rc.getTeam();
        enemy = me.opponent();
        dir = Direction.NORTH;
        dist = 20;
        cowGrowth = myRC.senseCowGrowth();
        distInDirs = new int[8];
        myLoc = rc.getLocation();

        // Determine what clan supposed to be in, assign self gid.
        // TODO 10 should be constant
        // TODO We assume only one builder clan here. Maybe use clan private memory to fix.
        for (int i = 0; i < Channels.MAX_ROBOTS; i += 10) {
            int pclan = i / 10;
            ClanMode mode = Clans.getClanMode(pclan);
            if (mode == ClanMode.DEFENDER) {
                clan = pclan;
                for (int pgidOffset = 0; pgidOffset < 10; pgidOffset++) {
                    int pgid = i + pgidOffset;
                    if (Liveness.getLastPostedRoundByGid(pgid) == 0) {
                        gid = pgid;
                        Liveness.updateLiveness(RobotType.NOISETOWER, gid);
                        Clans.setClanSize(clan, Clans.getClanSize(clan) + 1);
                        break;
                    }
                }
            }
        }
        assert (gid != -1);
        assert (clan != -1);
    }

    protected void getUpdates() {
        // pass
    }

    protected void updateInternals() {
        myPastrs = rc.sensePastrLocations(me);
        enemyPastrs = rc.sensePastrLocations(enemy);

        MapLocation oldPastr = myPastr;

        myPastr = null;
        enemyPastr = null;

        // Pick the nearest pastr
        if (myPastrs.length > 0) {
            myPastr = myPastrs[0];
            for (MapLocation p : myPastrs) {
                myPastr = p.distanceSquaredTo(myLoc) < myPastr.distanceSquaredTo(myLoc) ? p
                        : myPastr;
            }
        }

        if (!myPastr.equals(oldPastr)) {
            for (int x = 0; x < 8; x++) {
                distInDirs[x] = checkDirection(myPastr, directions[x]);
            }
        }

        // Pick the farthest in range
        if (enemyPastrs.length > 0 && myPastr == null) {
            enemyPastr = enemyPastrs[0];
            for (MapLocation p : enemyPastrs) {
                int distToEnemy = p.distanceSquaredTo(myLoc);
                enemyPastr = distToEnemy > enemyPastr.distanceSquaredTo(myLoc)
                        && distToEnemy <= 300 ? p : enemyPastr;
            }
        }
        target = null;
    }

    public void doAction() throws GameActionException {
        // Offense
        if (enemyPastr != null) {
            // Be offensive only if they are in range
            if (rc.canAttackSquare(enemyPastr)) {
                target = enemyPastr;
            }
        }

        // Leave this code here since we only want it to run whenever the NT is active
        if (myPastr != null) {
            if (dist <= 5) {
                dist -= 1;
            }
            else {
                dist -= 2;
            }
            if (dist < 3) {
                dir = dir.rotateRight();
                dir = dir.rotateRight();
                dir = dir.rotateRight();

                for (int x = 0; x < 9; x++) {
                    int dirDist = distInDirs[dir.ordinal()];
                    if (dirDist < 4) {
                        dir = dir.rotateRight();
                        dir = dir.rotateRight();
                        dir = dir.rotateRight();
                    }
                    else {
                        dist = dirDist;
                        break;
                    }
                }

                // This tries to handle the off the map situation
                while (!rc.canAttackSquare(myPastr.add(dir, dist)) && dist > 0) {
                    dist -= 1;
                }
            }
            target = myPastr.add(dir, dist);
        }

        // We have no use and are just hurting our spawn delay
        if (target == null) {
            rc.selfDestruct();
        }

        if (rc.canAttackSquare(target)) {
            rc.attackSquare(target);
        }
    }

    protected void sendUpdates() throws GameActionException {
        Liveness.updateLiveness(RobotType.NOISETOWER, gid);
    }

    protected void doCompute() {
        // pass
    }

    public int checkDirection(MapLocation pastr, Direction dir) {
        int d = 1;
        MapLocation target = pastr.add(dir, d);

        while (rc.canAttackSquare(target)) {
            TerrainTile terrain = rc.senseTerrainTile(target);
            if (terrain == TerrainTile.OFF_MAP) {
                break;
            }
            if (isValidTerrain(target)) {
                target = target.add(dir);
            }
            else {
                Direction left = dir.rotateLeft();
                Direction right = dir.rotateRight();
                MapLocation left1 = target.add(left);
                MapLocation right1 = target.add(right);
                MapLocation left2 = target.add(left.rotateLeft());
                MapLocation right2 = target.add(right.rotateRight());

                if ((isValidTerrain(left1) && isValidTerrain(left2))
                        || (isValidTerrain(right1) && isValidTerrain(right2))) {
                    target = pastr.add(dir, d);
                }
                else {
                    // Otherwise, cows will not get around
                    return d;
                }
            }
            d++;
        }
        return d;
    }

    private boolean isValidTerrain(MapLocation m) {
        TerrainTile t = rc.senseTerrainTile(m);
        return (t == TerrainTile.NORMAL || t == TerrainTile.ROAD) && cowGrowth[m.x][m.y] > 0.0;
    }

}
