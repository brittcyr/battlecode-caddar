package new_dijkstra;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;

public class HQRobot extends BaseRobot {

    public HQRobot(RobotController myRC) throws GameActionException {
        super(myRC);
    }

    public void run() {
        try {
            boolean doSpawn = false;
            Direction toEnemy = null;
            // Check if a robot is spawnable and spawn one if it is
            if (rc.isActive() && rc.senseRobotCount() < GameConstants.MAX_ROBOTS) {
                toEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
                if (rc.senseObjectAtLocation(rc.getLocation().add(toEnemy)) == null) {
                    doSpawn = true;
                }
            }

            if (doSpawn) {
                rc.spawn(toEnemy);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("HQ Exception");
        }
    }
}
