package pastr_attacker;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
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
            // TODO organize the team
            int attackers = rc.readBroadcast(1000);
            rc.broadcast(1000, 0);
            if (attackers < 2) {
                // Tell 1001 we need an attacker
                rc.broadcast(1001, 1);
            }
            else {
                rc.broadcast(1001, 0);
            }

            // Check if a robot is spawnable and spawn one if it is
            if (rc.isActive() && rc.senseRobotCount() < GameConstants.MAX_ROBOTS) {
                Direction toEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
                if (rc.senseObjectAtLocation(rc.getLocation().add(toEnemy)) == null) {
                    rc.spawn(toEnemy);
                    return;
                }
            }

            // Attack enemies
            Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 10, rc.getTeam()
                    .opponent());
            for (int x = 0; x < nearbyEnemies.length; x++) {
                RobotInfo robotInfo = rc.senseRobotInfo(nearbyEnemies[x]);
                if (robotInfo.type == RobotType.SOLDIER) {
                    rc.attackSquare(robotInfo.location);
                    return;
                }
            }

        }
        catch (Exception e) {
            System.out.println("HQ Exception");
        }
    }

}
