package bug_bot;

import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class CowboyRobot extends BaseRobot {
    static Random      rand;
    static Direction[] directions = { Direction.NORTH, Direction.NORTH_EAST, Direction.EAST,
            Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST,
            Direction.NORTH_WEST };

    public CowboyRobot(RobotController myRC) throws GameActionException {
        super(myRC);
        rand = new Random(myRC.getRobot().getID());
    }

    public void run() {
        try {
            // Attack if possible prioritize attacking the enemy robots first
            Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 10, rc.getTeam()
                    .opponent());
            for (int x = 0; x < nearbyEnemies.length; x++) {
                RobotInfo robotInfo = rc.senseRobotInfo(nearbyEnemies[x]);
                if (robotInfo.type == RobotType.SOLDIER) {
                    rc.attackSquare(robotInfo.location);
                    return;
                }
            }
            for (int x = 0; x < nearbyEnemies.length; x++) {
                RobotInfo robotInfo = rc.senseRobotInfo(nearbyEnemies[x]);
                if (robotInfo.type != RobotType.HQ) {
                    rc.attackSquare(robotInfo.location);
                    return;
                }
            }

            // Construct a PASTR if the square is really good
            if (rc.senseCowsAtLocation(rc.getLocation()) > 2000
                    && rc.senseNearbyGameObjects(Robot.class, 30, rc.getTeam().opponent()).length == 0) {
                rc.construct(RobotType.PASTR);
                return;
            }

            // Construct a PASTR randomly
            int action = (rc.getRobot().getID() * rand.nextInt(101) + 50) % 101;
            if (action < 1 && rc.getLocation().distanceSquaredTo(rc.senseHQLocation()) > 20
                    && rc.senseCowGrowth()[rc.getLocation().x][rc.getLocation().y] > 0
                    && rc.senseRobotCount() > 5) {
                rc.construct(RobotType.PASTR);
                return;
            }

            int t = rc.readBroadcast(0);
            if (t == 99999) {
                Direction moveDirection = directions[rand.nextInt(8)];
                if (rc.canMove(moveDirection)) {
                    rc.move(moveDirection);
                }
                return;
            }

            MapLocation target = new MapLocation(t / 100, t % 100);

            // Otherwise try to move to the target
            BugNavigator.navigateTo(rc, target);

        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println(e);
            System.out.println("Soldier Exception");
        }
    }
}
