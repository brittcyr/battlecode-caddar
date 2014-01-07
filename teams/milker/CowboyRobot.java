package milker;

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
        // TODO Auto-generated constructor stub
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

            int action = (rc.getRobot().getID() * rand.nextInt(101) + 50) % 101;

            if (rc.senseCowsAtLocation(rc.getLocation()) > 1500
                    && rc.senseNearbyGameObjects(Robot.class, 30, rc.getTeam().opponent()).length == 0
                    && rc.senseNearbyGameObjects(Robot.class, 2, rc.getTeam()).length == 0) {
                rc.broadcast(10, 1);
                rc.construct(RobotType.PASTR);
                return;
            }

            // Construct a PASTR
            if (action < 1 && rc.getLocation().distanceSquaredTo(rc.senseHQLocation()) > 20
                    && rc.senseCowGrowth()[rc.getLocation().x][rc.getLocation().y] > 0
                    && rc.senseNearbyGameObjects(Robot.class, 4, rc.getTeam()).length == 0
                    && rc.senseRobotCount() > 5) {
                rc.construct(RobotType.PASTR);
            }
            else if (action < 70) {
                // Random movement
                Direction moveDirection = directions[rand.nextInt(8)];
                if (rc.canMove(moveDirection)) {
                    rc.sneak(moveDirection);
                }
            }
            else {
                // Sneak towards the milk
                if (rc.readBroadcast(10) == 1) {
                    int milk_int = rc.readBroadcast(1);

                    MapLocation milk = new MapLocation(milk_int / 100, milk_int % 100);
                    Direction toMilk = rc.getLocation().directionTo(milk);

                    if (rc.getLocation().distanceSquaredTo(milk) < 3) {
                        rc.construct(RobotType.PASTR);
                        return;
                    }

                    if (rc.canMove(toMilk)) {
                        rc.sneak(toMilk);
                    }
                }
                else {
                    Direction toEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
                    if (rc.canMove(toEnemy)) {
                        rc.sneak(toEnemy);
                    }
                }
            }

        }
        catch (Exception e) {
            System.out.println("Soldier Exception");
        }
    }
}
