package bug_bot;

import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.TerrainTile;

public class CowboyRobot extends BaseRobot {
    static Random      rand;
    static Direction[] directions                  = { Direction.NORTH, Direction.NORTH_EAST,
            Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST,
            Direction.WEST, Direction.NORTH_WEST  };
    boolean            bugging                     = false;
    double             dist_to_target_at_bug_start = 0.0;
    MapLocation        last_wall                   = null;
    MapLocation        last_square                 = null;
    int                direction_to_turn           = 1;

    public CowboyRobot(RobotController myRC) throws GameActionException {
        super(myRC);
        rand = new Random();
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

            MapLocation target = rc.senseEnemyHQLocation();

            // If we are free from a wall
            if (!bugging) {
                // Sneak towards the enemy
                Direction toTarget = rc.getLocation().directionTo(target);
                if (rc.canMove(toTarget)) {
                    last_square = rc.getLocation();
                    rc.sneak(toTarget);
                    return;
                }
                TerrainTile next = rc.senseTerrainTile(rc.getLocation().add(toTarget));
                if (next == TerrainTile.VOID || next == TerrainTile.OFF_MAP) {
                    last_wall = rc.getLocation().add(toTarget);
                    dist_to_target_at_bug_start = rc.getLocation().distanceSquaredTo(target);

                    // TODO: set direction to turn
                    double dist_plus = 99999.0;
                    double dist_minus = 99999.0;
                    for (int x = 1; x < 4; x++) {
                        MapLocation possible = rc.getLocation().add(
                                directions[(toTarget.ordinal() + x) % 8]);
                        TerrainTile tile = rc.senseTerrainTile(possible);
                        if (tile == TerrainTile.NORMAL || tile == TerrainTile.ROAD) {
                            dist_plus = possible.distanceSquaredTo(target);
                            break;
                        }
                    }
                    for (int x = 1; x < 4; x++) {
                        MapLocation possible = rc.getLocation().add(
                                directions[(toTarget.ordinal() - x) % 8]);
                        TerrainTile tile = rc.senseTerrainTile(possible);
                        if (tile == TerrainTile.NORMAL || tile == TerrainTile.ROAD) {
                            dist_minus = possible.distanceSquaredTo(target);
                            break;
                        }
                    }
                    if (dist_minus < dist_plus) {
                        direction_to_turn = -1;
                    }

                    bugging = true;
                }
            }

            // Check if we should stop following wall
            if (bugging) {
                // if we are closer than when we started
                if (rc.getLocation().distanceSquaredTo(target) < dist_to_target_at_bug_start) {
                    bugging = false;
                }
            }

            if (bugging) {
                // Take the direction to the wall and add one until valid terrain, then move that
                // way
                MapLocation next_square = null;
                // TODO Need to be smarter about checking direction to bug
                for (int x = 1; x < 8; x++) {
                    Direction to_check = rc.getLocation().directionTo(last_wall);
                    next_square = rc.getLocation().add(
                            directions[(to_check.ordinal() + x * direction_to_turn) % 8]);
                    if (rc.senseTerrainTile(next_square) == TerrainTile.VOID
                            || rc.senseTerrainTile(next_square) == TerrainTile.OFF_MAP) {
                        last_wall = next_square;
                    }
                    else {
                        break;
                    }
                }
                Direction toTarget = rc.getLocation().directionTo(next_square);
                if (rc.canMove(toTarget)) {
                    last_square = rc.getLocation();
                    rc.sneak(toTarget);
                    return;
                }

            }

        }
        catch (Exception e) {
            System.out.println("Soldier Exception");
        }
    }
}
