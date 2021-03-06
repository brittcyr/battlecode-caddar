package pastr_attacker;

import battlecode.common.*;

public abstract class BaseRobot {
    public final RobotController rc;

    public BaseRobot(RobotController myRC) throws GameActionException {
        rc = myRC;
    }

    public void loop() {
        run();
        rc.yield();
    }

    public void run() {
        // pass
    }

}
