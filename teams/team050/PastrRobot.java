package team050;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class PastrRobot extends BaseRobot {

    public PastrRobot(RobotController myRC) throws GameActionException {
        super(myRC);
    }

    protected void getUpdates() {
        // Read the radio and see what needs computing
    }

    protected void updateInternals() {
        // This is where we should decide if we call for reinforcements
    }

    public void doAction() throws GameActionException {
        // New specs make self destruct really bad
    }

    protected void sendUpdates() throws GameActionException {
        // Send out the results of computing and send whether are under attack
        super.sendUpdates();
    }

    protected void doCompute() {
        // help with dijkstra
    }

}
