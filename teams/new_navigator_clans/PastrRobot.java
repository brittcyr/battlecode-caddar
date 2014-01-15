package new_navigator_clans;

import battlecode.common.GameActionException;
import battlecode.common.Robot;
import battlecode.common.RobotController;

public class PastrRobot extends BaseRobot {

    public PastrRobot(RobotController myRC) throws GameActionException {
        super(myRC);
    }

    protected void getUpdates() {
        // pass
    }

    protected void updateInternals() {
        // pass
    }

    public void doAction() throws GameActionException {
        if (rc.getHealth() <= 30.5 * rc.senseNearbyGameObjects(Robot.class, 2, rc.getTeam()
                .opponent()).length) {
            rc.selfDestruct();
        }
    }

    protected void sendUpdates() {
        // pass
    }

    protected void doCompute() {
        // pass
    }

}
