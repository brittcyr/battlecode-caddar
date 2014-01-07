package shepherd;

import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
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
    private Direction  primaryDirection;
    final int          spread     = 3;  // Should be an odd number. 5 = nw to ne if enemyhq=n.


    public CowboyRobot(RobotController myRC) throws GameActionException {
        super(myRC);
        rand = new Random();
        
        Direction toEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
        int toEnemyIndex = 0;
        for (int i = 0; i < directions.length; i++)
            if (directions[i] == toEnemy)
                toEnemyIndex = i;

        int spreadDir = (Rng.nextInt(spread) * rc.getRobot().getID()) % spread;
        spreadDir -= spread / 2;
        primaryDirection = directions[(toEnemyIndex + spreadDir) % directions.length];
        System.out.println("Primary direction: " + primaryDirection);
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

            // Construct a PASTR
            if (rc.getLocation().distanceSquaredTo(rc.senseHQLocation()) > 5  // TODO: map-dependent
                    && sufficientNearbyCows()) {
                rc.construct(RobotType.PASTR);
            }
            else if (action < 20) {
                // Random movement
                Direction moveDirection = directions[rand.nextInt(8)];
                if (rc.canMove(moveDirection)) {
                    rc.move(moveDirection);
                }
            }
            else {
                // Sneak in direction
                if (rc.canMove(primaryDirection)) {
                    rc.sneak(primaryDirection);
                }
            }

        }
        catch (Exception e) {
            System.out.println("Soldier Exception");
        }
    }

    private static boolean sufficientNearbyCows() {    // TODO: PASTR_RANGE is circle not bounding box
        final int sqLength = GameConstants.PASTR_RANGE;  // Length of bounding box to search in around curPos
        final double cowThreshold = 3000;
        double totalCowsNearby = 0;
        int[] curPos = {rc.getLocation().x - sqLength/2, rc.getLocation().y - sqLength/2};
        for (int x = 0; x < sqLength; x++) {
            for (int y = 0; y < sqLength; y++) {
                try {
                    totalCowsNearby += rc.senseCowsAtLocation(new MapLocation(curPos[0] + x,
                            curPos[1] + y));
                    if (totalCowsNearby > cowThreshold) {
                        return true;
                    }
                }
                catch (GameActionException e) {
                    return false;
                }
            }
        }
        return false;
    }
}
        
