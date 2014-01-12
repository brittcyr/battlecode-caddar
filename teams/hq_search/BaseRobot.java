package hq_search;

import battlecode.common.*;

public abstract class BaseRobot {
    public final RobotController rc;

    public BaseRobot(RobotController myRC) throws GameActionException {
        rc = myRC;
    }

    public void loop() {
        if (rc.isActive() || rc.getType() == RobotType.HQ) {
            run();
        }
        rc.yield();
    }

    public void run() {
        // pass
    }

}
