package new_dijkstra;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public abstract class BaseRobot {
    public final RobotController rc;

    public BaseRobot(RobotController myRC) throws GameActionException {
        rc = myRC;
    }

    public void loop() throws GameActionException {
        if (rc.isActive() || rc.getType() == RobotType.HQ) {
            run();
        }
        rc.yield();
    }

    public void run() throws GameActionException {
        // pass
    }

}
