package pastr_attacker;

import battlecode.common.GameActionException;
import battlecode.common.Robot;
import battlecode.common.RobotController;

public class PastrRobot extends BaseRobot {
    private double lastHealth;

    public PastrRobot(RobotController myRC) throws GameActionException {
        super(myRC);
        lastHealth = myRC.getHealth();
    }

    public void run() {
        if (rc.getHealth() <= 10.5 * rc.senseNearbyGameObjects(Robot.class, 5, rc.getTeam()
                .opponent()).length) {
            try {
                rc.selfDestruct();
            }
            catch (GameActionException e) {
            }
        }
        if (lastHealth > rc.getHealth() * 2.01) {
            try {
                rc.selfDestruct();
            }
            catch (GameActionException e) {
            }
        }
        if (rc.getHealth() < 11.0) {
            try {
                rc.selfDestruct();
            }
            catch (GameActionException e) {
            }
        }
        lastHealth = rc.getHealth();

    }

}
