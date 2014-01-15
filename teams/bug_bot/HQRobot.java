package bug_bot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import bug_bot.rpc.Clans;
import bug_bot.rpc.Clans.ClanMode;

public class HQRobot extends BaseRobot {

    public HQRobot(RobotController myRC) throws GameActionException {
        super(myRC);
        Clans.setWaypoint(0, rc.senseHQLocation());
    }

    public void run() throws GameActionException {

        try {
            // Manage clan waypoints.
            for (int i = 0; i < Clans.getNumClans(); i++) {
                switch (Clans.getClanMode(i)) {
                    case DEAD:
                        break;
                    case IDLE:
                        if (Clans.getSize(i) >= 5) {
                            Clans.setClanMode(i, ClanMode.RAIDER);
                            Clans.setWaypoint(i, rc.senseEnemyHQLocation());
                        } else {
                            Clans.setWaypoint(i, rc.senseHQLocation());
                            break;
                        }
                    default:
                        break;
                }
            }

            Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 15, rc.getTeam()
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
