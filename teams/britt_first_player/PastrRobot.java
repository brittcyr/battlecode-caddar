package britt_first_player;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class PastrRobot extends BaseRobot {

    public PastrRobot(RobotController myRC) throws GameActionException {
        super(myRC);
        // TODO Auto-generated constructor stub
    }

    public void run() {
        if (rc.getHealth() < 20) {
            try {
                rc.selfDestruct();
            }
            catch (GameActionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

}
