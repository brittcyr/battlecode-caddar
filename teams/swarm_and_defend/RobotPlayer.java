package swarm_and_defend;

import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

/*
 * This program makes the cowboys swarm around its base. If a cowboy finds a good patch to milk cows
 * on, it converts to PASTR.
 * 
 * Later in the game, if we have enough robots, HQ sends half of them to raid PASTRs.
 */
public class RobotPlayer {

    private static Direction[]     directions = { Direction.NORTH, Direction.NORTH_EAST,
            Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST,
            Direction.WEST, Direction.NORTH_WEST };

    private static RobotController rc;
    
    private static Random          rand       = new Random();

    private static boolean         initializedWayPoint = false;

    public static void run(RobotController rc) {
        RobotPlayer.rc = rc;
        rand.setSeed(rc.getRobot().getID());

        while (true) {
            try {
                switch (rc.getType()) {
                    case HQ:
                        while (true) {
                            handleHq();
                            rc.yield();
                        }
                    case SOLDIER:
                        while (true) {
                            handleSoldier();
                            rc.yield();
                        }
                    case NOISETOWER:
                        while (true) {
                            handleNoiseTower();
                            rc.yield();
                        }
                    case PASTR:
                        while (true) {
                            handlePastr();
                            rc.yield();
                        }
                    default:
                        return;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void handleNoiseTower() {
        while (true) {
            rc.yield();
        }
    }

    private static void handlePastr() {
        while (true) {
            rc.yield();
        }
    }

    private static void handleSoldier() throws GameActionException {
        // Offense.
        // Attack if possible prioritize attacking the enemy robots first
        Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 10, rc.getTeam().opponent());
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

        // Movement.
        // Get waypoint.
        int wpi = rc.readBroadcast(0);
        MapLocation wp = new MapLocation(wpi / 100, wpi % 100);

        // Add some randomness
        int swarmRadius = 5;
        wp = wp.add(rand.nextInt(swarmRadius) - swarmRadius / 2, rand.nextInt(swarmRadius)
                - swarmRadius / 2);

        // Sometimes go to waypoint, sometimes wander.
        if (rc.isActive()) {
            if (rand.nextDouble() < 0.4) {
                rc.move(directions[rand.nextInt(directions.length)]);
            }
            else {
                rc.move(rc.getLocation().directionTo(wp));
            }
        }
    }

    private static void handleHq() throws GameActionException {
        if (!initializedWayPoint) {
            // Set waypoint to home base.
            MapLocation hq = rc.senseHQLocation();
            rc.broadcast(0, hq.x * 100 + hq.y);
            initializedWayPoint = true;
        }

        if (rc.senseNearbyGameObjects(Robot.class, 10).length < 10) {
            // Spawn soldiers as fast as possible.
            if (rc.isActive()) {
                int dIndex = rand.nextInt(8);
                while (rc.senseObjectAtLocation(rc.getLocation().add(directions[dIndex])) != null) {
                    dIndex = (dIndex + 1) % directions.length;
                }
                rc.spawn(directions[dIndex]);
            }
        }
        else {
            // Zerg rush enemy hq.
            MapLocation enemyHq = rc.senseEnemyHQLocation();
            rc.broadcast(0, enemyHq.x * 100 + enemyHq.y);
        }
    }
}
