package shepherd;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class PastrRobot extends BaseRobot {

    public PastrRobot(RobotController myRC) throws GameActionException {
        super(myRC);
    }

    public void run() {
        if (rc.getHealth() < 20) {
            try {
                rc.selfDestruct();
            }
            catch (GameActionException e) {
                e.printStackTrace();
            }
        }

    }

}
