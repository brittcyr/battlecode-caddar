package bug_bot;

import battlecode.common.GameActionException;
import bug_bot.rpc.Radio;
import battlecode.common.RobotController;

public abstract class BaseRobot {
    public final RobotController rc;

    public BaseRobot(RobotController myRC) throws GameActionException {
        rc = myRC;
        Radio.setRobotController(rc);
    }

    public void loop() throws GameActionException {
        if (rc.isActive()) {
            run();
        }
        rc.yield();
    }

    public void run() throws GameActionException {
        // pass
    }

}
