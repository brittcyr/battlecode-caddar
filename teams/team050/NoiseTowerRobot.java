package team050;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.Team;
import battlecode.common.TerrainTile;

public class NoiseTowerRobot extends BaseRobot {
    public final Team  me;
    public final Team  enemy;
    public MapLocation myPastr;
    public MapLocation enemyPastr;
    public MapLocation target;
    public Direction   dir;
    public int         dist;

    public NoiseTowerRobot(RobotController myRC) throws GameActionException {
        super(myRC);
        me = rc.getTeam();
        enemy = me.opponent();
        dir = Direction.NORTH;
        dist = 20;
    }

    protected void getUpdates() {
        // pass
    }

    protected void updateInternals() {
        // TODO: Compute how to bring in cows in unusual maps
    }

    public void doAction() throws GameActionException {
        MapLocation[] myPastrs = rc.sensePastrLocations(me);
        MapLocation[] enemyPastrs = rc.sensePastrLocations(enemy);
        // If we don't see anything, then selfdestruct to help with spawn rate
        if (enemyPastrs.length == 0 && myPastrs.length == 0) {
            Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 2, enemy);
            Robot[] nearbyFriendlies = rc.senseNearbyGameObjects(Robot.class, 2, me);
            if (nearbyEnemies.length >= nearbyFriendlies.length) {
                rc.selfDestruct();
            }
        }

        if (myPastrs.length > 0) {
            // Pick the nearest pastr
            myPastr = myPastrs[0];
            for (MapLocation p : myPastrs) {
                myPastr = p.distanceSquaredTo(rc.getLocation()) < myPastr.distanceSquaredTo(rc
                        .getLocation()) ? p : myPastr;
            }

            dist -= 4;
            if (dist < 4) {
                dir = dir.rotateRight();
                dir = dir.rotateRight();
                dir = dir.rotateRight();

                for (int x = 0; x < 9; x++) {
                    if (!checkDirection(myPastr, dir)) {
                        dir = dir.rotateRight();
                        dir = dir.rotateRight();
                        dir = dir.rotateRight();
                    }
                    else {
                        break;
                    }
                }

                dist = 15;
                // This tries to handle the off the map situation
                while (!rc.canAttackSquare(myPastr.add(dir, dist)) && dist > 0) {
                    dist -= 1;
                }
            }

            target = myPastr.add(dir, dist);
        }

        if (enemyPastrs.length > 0) {
            enemyPastr = enemyPastrs[0];
            for (MapLocation p : enemyPastrs) {
                int distToEnemy = p.distanceSquaredTo(rc.getLocation());
                enemyPastr = distToEnemy > enemyPastr.distanceSquaredTo(rc.getLocation())
                        && distToEnemy <= 300 ? p : enemyPastr;
            }
            // Be offensive
            if (rc.canAttackSquare(enemyPastr)) {
                target = enemyPastr;
            }
        }

        if (rc.canAttackSquare(target)) {
            rc.attackSquare(target);
        }
    }

    protected void sendUpdates() {
        // pass
    }

    protected void doCompute() {
        // pass
        // TODO: Do spare computing for offense
    }

    public boolean checkDirection(MapLocation pastr, Direction dir) {
        MapLocation target = pastr;

        while (rc.canAttackSquare(target)) {
            TerrainTile terrain = rc.senseTerrainTile(target);
            if (terrain == TerrainTile.OFF_MAP) {
                break;
            }
            if (terrain == TerrainTile.NORMAL || terrain == TerrainTile.ROAD) {
                target = target.add(dir);
            }
            else {
                MapLocation left = target.add(dir.rotateLeft().rotateLeft());
                MapLocation right = target.add(dir.rotateRight().rotateRight());

                TerrainTile leftTerrain = rc.senseTerrainTile(left);
                TerrainTile rightTerrain = rc.senseTerrainTile(right);

                if (leftTerrain == TerrainTile.NORMAL || leftTerrain == TerrainTile.ROAD
                        || rightTerrain == TerrainTile.NORMAL || rightTerrain == TerrainTile.ROAD) {
                    target = target.add(dir);
                }
                else {
                    // Otherwise we are on a void (maybe off the map, need to test) and left and
                    // right are as well
                    return false;
                }
            }
        }
        return true;
    }

}
