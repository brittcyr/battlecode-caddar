package team050;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
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

    }

    public void doAction() throws GameActionException {
        MapLocation[] myPastrs = rc.sensePastrLocations(me);
        MapLocation[] enemyPastrs = rc.sensePastrLocations(enemy);

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
    }

}
