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
        Direction toSpawn = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
        // Check if a robot is spawnable and spawn one if it is
        if (rc.isActive() && rc.senseRobotCount() < GameConstants.MAX_ROBOTS) {
            for (int rot = 0; rot < 8; rot++) {
                if (rc.senseObjectAtLocation(rc.getLocation().add(toSpawn)) == null) {
                    doSpawn = true;
                    break;
                }
                toSpawn = toSpawn.rotateLeft();
            }
        }

        // TODO: Insert attack code here

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

}
