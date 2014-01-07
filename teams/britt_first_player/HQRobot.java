package britt_first_player;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class HQRobot extends BaseRobot {

    public HQRobot(RobotController myRC) throws GameActionException {
        super(myRC);
        // TODO Auto-generated constructor stub
    }

    public void run() {
        try {
            Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 10, rc.getTeam()
                    .opponent());
            if (nearbyEnemies.length > 0) {
                RobotInfo robotInfo = rc.senseRobotInfo(nearbyEnemies[0]);
                if (robotInfo.type == RobotType.SOLDIER) {
                    rc.attackSquare(robotInfo.location);
                    return;
                }
            }
            // Check if a robot is spawnable and spawn one if it is
            if (rc.isActive() && rc.senseRobotCount() < 25) {
                Direction toEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
                if (rc.senseObjectAtLocation(rc.getLocation().add(toEnemy)) == null) {
                    rc.spawn(toEnemy);
                }
            }

            else {
                if (nearbyEnemies.length > 0) {
                    RobotInfo robotInfo = rc.senseRobotInfo(nearbyEnemies[0]);
                    if (robotInfo.type == RobotType.SOLDIER) {
                        rc.attackSquare(robotInfo.location);
                    }
                }
            }
        }
        catch (Exception e) {
            System.out.println("HQ Exception");
        }
    }
}
