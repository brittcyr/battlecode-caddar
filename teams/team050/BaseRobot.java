package team050;

import team050.rpc.Radio;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public abstract class BaseRobot {
    protected final RobotController rc;

    public BaseRobot(RobotController myRC) throws GameActionException {
        rc = myRC;
        Radio.setRobotController(myRC);
    }

    /*
     * This is the loop that is called by RobotController which will run the actual code for robots
     */
    public void loop() throws GameActionException {
        // Get updates mostly just reading radio
        getUpdates();

        // This is for getting our waypoints updated and changing any needed robot state
        updateInternals();

        // Compute and do whatever action we wanted for that turn
        if (rc.isActive()) {
            doAction();
        }

        // Tell others.
        sendUpdates();

        // Use spare compute cycles if possible
        doCompute();

        rc.yield();
    }

    /*
     * Listen to the radio to see if targets and clan types have changed
     */
    protected abstract void getUpdates() throws GameActionException;

    /*
     * Decide what we are going to do on our action
     */
    protected abstract void updateInternals() throws GameActionException;

    /*
     * Do the actual robot action
     */
    protected abstract void doAction() throws GameActionException;

    /*
     * Send the results of what we have done or computed
     */
    protected abstract void sendUpdates() throws GameActionException;

    /*
     * Do Computation, mostly used for PASTR and HQ to calculate maps for cowboys
     */
    protected abstract void doCompute() throws GameActionException;
}
