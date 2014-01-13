package rpcb;

import java.util.Random;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public abstract class BaseRobot {
    public static RobotController rc;
    public final static Random    rand = new Random();

    public BaseRobot(RobotController myRC) throws GameActionException {
        rc = myRC;
    }

    public void loop() {
        if (rc.isActive()) {
            run();
        }
        rc.yield();
    }

    public void run() {
        // pass
    }

}
