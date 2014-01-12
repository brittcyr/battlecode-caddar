package pastr_attacker;

import battlecode.common.*;

public abstract class BaseRobot {
    public final RobotController rc;

    public BaseRobot(RobotController myRC) throws GameActionException {
        rc = myRC;
    }

    public void loop() {
        // TODO: change this model to
        // 1. Pre-action (update radio and similar)
        // 2. Action (move or attack)
        // 3. Spend spare cycles on computation
        run();
        rc.yield();
    }

    public void run() {
        // pass
    }

}
