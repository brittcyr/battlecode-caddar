package team050;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class PastrRobot extends BaseRobot {

    public PastrRobot(RobotController myRC) throws GameActionException {
        super(myRC);
    }

    protected void getUpdates() {
    	// TODO: Check if there are any requests for computation
    }

    protected void updateInternals() {
    	// TODO: Potential distress signal
        // This is where we should decide if we call for reinforcements
    }

    public void doAction() throws GameActionException {
    	GeneralNavigation.prepareCompute(rc, rc.getLocation());
    	GeneralNavigation.doCompute();
    }

    protected void sendUpdates() {
    	// TODO: send the results
    }

    protected void doCompute() {
    	// pass
    	// The action of pastr is just computing
    }

}