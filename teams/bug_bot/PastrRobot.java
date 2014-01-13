package bug_bot;

import battlecode.common.GameActionException;
import battlecode.common.Robot;
import battlecode.common.RobotController;

public class PastrRobot extends BaseRobot {
    private double lastHealth;

    public PastrRobot(RobotController myRC) throws GameActionException {
        super(myRC);
        lastHealth = myRC.getHealth();
        // TODO Auto-generated constructor stub
    }

    public void run() throws GameActionException {
        if (rc.getHealth() <= 10.5 * rc.senseNearbyGameObjects(Robot.class, 5, rc.getTeam()
                .opponent()).length) {
            rc.selfDestruct();
        }
        if (lastHealth > rc.getHealth() * 2.01) {
            rc.selfDestruct();
        }
        if (rc.getHealth() < 11.0) {
            rc.selfDestruct();
        }
        lastHealth = rc.getHealth();

    }

}
