package new_compute_model;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;

public class HQRobot extends BaseRobot {

    public HQRobot(RobotController myRC) throws GameActionException {
        super(myRC);
    }

    protected void getUpdates() {
        // pass
    }

    protected void updateInternals() {
        // pass
    }

    protected void doAction() throws GameActionException {
        boolean doSpawn = false;
        Direction toEnemy = null;
        // Check if a robot is spawnable and spawn one if it is
        if (rc.isActive() && rc.senseRobotCount() < GameConstants.MAX_ROBOTS) {
            toEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
            if (rc.senseObjectAtLocation(rc.getLocation().add(toEnemy)) == null) {
                doSpawn = true;
            }
        }

        // TODO: Insert attack code here

        if (doSpawn) {
            rc.spawn(toEnemy);
        }
    }

    protected void sendUpdates() {
        // pass
    }

    protected void doCompute() {
        // pass
    }

}
