package team050;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.Team;
import battlecode.common.TerrainTile;

public class NoiseTowerRobot extends BaseRobot {
    public final Team    me;
    public final Team    enemy;
    public MapLocation   myPastr;
    public MapLocation   enemyPastr;
    public MapLocation   target;
    public MapLocation[] myPastrs;
    public MapLocation[] enemyPastrs;
    public Direction     dir;
    public int           dist;
    public double[][]    cowGrowth;

    public NoiseTowerRobot(RobotController myRC) throws GameActionException {
        super(myRC);
        me = rc.getTeam();
        enemy = me.opponent();
        dir = Direction.NORTH;
        dist = 20;
        cowGrowth = myRC.senseCowGrowth();
    }

    protected void getUpdates() {
        // pass
    }

    protected void updateInternals() {
        myPastrs = rc.sensePastrLocations(me);
        enemyPastrs = rc.sensePastrLocations(enemy);

        myPastr = null;
        enemyPastr = null;

        // Pick the nearest pastr
        if (myPastrs.length > 0) {
            myPastr = myPastrs[0];
            for (MapLocation p : myPastrs) {
                myPastr = p.distanceSquaredTo(rc.getLocation()) < myPastr.distanceSquaredTo(rc
                        .getLocation()) ? p : myPastr;
            }
        }

        // Pick the farthest in range
        if (enemyPastrs.length > 0) {
            enemyPastr = enemyPastrs[0];
            for (MapLocation p : enemyPastrs) {
                int distToEnemy = p.distanceSquaredTo(rc.getLocation());
                enemyPastr = distToEnemy > enemyPastr.distanceSquaredTo(rc.getLocation())
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
            dist -= 2;
            if (dist < 3) {
                dir = dir.rotateRight();
                dir = dir.rotateRight();
                dir = dir.rotateRight();

                for (int x = 0; x < 9; x++) {
                    int dirDist = checkDirection(myPastr, dir);
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
        super.sendUpdates();
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
                MapLocation left1 = target.add(dir.rotateLeft());
                MapLocation right1 = target.add(dir.rotateRight());
                MapLocation left2 = target.add(dir.rotateLeft().rotateLeft());
                MapLocation right2 = target.add(dir.rotateRight().rotateRight());

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
