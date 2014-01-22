package team050;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.Team;

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
        // TODO: Check which lines are valid to bring in cows
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

        if (enemyPastrs.length > 0) {
            // Be offensive
            target = enemyPastrs[0];
        }
        if (myPastrs.length > 0) {
            myPastr = myPastrs[0];
            dist -= 5;
            if (dist < 5) {
                dir = dir.rotateRight();
                dir = dir.rotateRight();
                dir = dir.rotateRight();
                dist = 15;
            }
            target = myPastr.add(dir, dist);
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

}
