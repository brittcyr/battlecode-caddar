package team050;

import java.util.Random;

import team050.rpc.Liveness;
import team050.rpc.Radio;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public abstract class BaseRobot {
    protected final RobotController rc;
    protected static Random         rand;

    public BaseRobot(RobotController myRC) throws GameActionException {
        rc = myRC;
        Radio.setRobotController(myRC);
        rand = new Random();
        rand.setSeed(rc.getRobot().getID());
    }

    public void loop() throws GameActionException {
        // TODO: Apply Clock time checks into each step and make estimates for each length

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

    protected abstract void doCompute();

    protected void sendUpdates() throws GameActionException {
        Liveness.updateLiveness(rc);
    }

    protected abstract void updateInternals() throws GameActionException;

    protected abstract void getUpdates() throws GameActionException;

    protected abstract void doAction() throws GameActionException;

}
