package new_compute_model;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public abstract class BaseRobot {
    protected final RobotController rc;

    public BaseRobot(RobotController myRC) throws GameActionException {
        rc = myRC;
    }

    public void loop() throws GameActionException {
        // TODO: Apply Clock time checks into each step and make estimates for each length

        // Get updates mostly just reading radio
        getUpdates();

        // This is for getting our waypoints updated and changing any needed robot state
        updateInternals();

        // Compute and do whatever action we wanted for that turn
        if (rc.isActive()) {
            try {
                doAction();
            }
            catch (GameActionException e) {
                e.printStackTrace();
            }
        }

        // Tell others
        sendUpdates();

        // Use spare compute cycles if possible
        doCompute();

        rc.yield();
    }

    protected abstract void doCompute();

    protected abstract void sendUpdates();

    protected abstract void updateInternals();

    protected abstract void getUpdates();

    protected abstract void doAction() throws GameActionException;

}
