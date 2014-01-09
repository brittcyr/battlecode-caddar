package bug_bot;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.TerrainTile;

public class BugNavigator {
    static Direction[] directions                  = { Direction.NORTH, Direction.NORTH_EAST,
            Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST,
            Direction.WEST, Direction.NORTH_WEST  };
    static boolean     bugging                     = false;
    static double      dist_to_target_at_bug_start = 0.0;
    static MapLocation last_wall                   = null;
    static int         direction_to_turn           = 1;

    public static void navigateTo(RobotController rc, MapLocation target) {
        try {
            Direction toTarget = rc.getLocation().directionTo(target);
            // If we are free from a wall
            if (!bugging) {
                if (rc.canMove(toTarget)) {
                    rc.sneak(toTarget);
                    return;
                }
                last_wall = rc.getLocation().add(toTarget);
                dist_to_target_at_bug_start = rc.getLocation().distanceSquaredTo(target);

                double dist_plus = 9999;
                double dist_minus = 9999;
                for (int x = 1; x < 4; x++) {
                    MapLocation possible = rc.getLocation().add(
                            directions[(toTarget.ordinal() + x + 8) % 8]);
                    TerrainTile tile = rc.senseTerrainTile(possible);
                    if (tile == TerrainTile.NORMAL || tile == TerrainTile.ROAD) {
                        dist_plus = possible.distanceSquaredTo(target);
                        break;
                    }
                }
                for (int x = 1; x < 4; x++) {
                    MapLocation possible = rc.getLocation().add(
                            directions[(toTarget.ordinal() - x + 8) % 8]);
                    TerrainTile tile = rc.senseTerrainTile(possible);
                    if (tile == TerrainTile.NORMAL || tile == TerrainTile.ROAD) {
                        dist_minus = possible.distanceSquaredTo(target);
                        break;
                    }
                }
                direction_to_turn = dist_minus < dist_plus ? -1 : 1;
                bugging = true;
            }

            // Check if we should stop following wall
            if (bugging) {
                // if we are closer than when we started
                if (rc.getLocation().distanceSquaredTo(target) < dist_to_target_at_bug_start) {
                    bugging = false;
                    // Call it again with navigating not in bugging mode
                    navigateTo(rc, target);
                    return;
                }

                // Take the direction to wall and add one until valid terrain, then move that way
                MapLocation next_square = null;
                for (int x = 1; x < 8; x++) {
                    Direction to_check = rc.getLocation().directionTo(last_wall);
                    next_square = rc.getLocation().add(
                            directions[(to_check.ordinal() + x * direction_to_turn + 8) % 8]);
                    if (rc.senseTerrainTile(next_square) == TerrainTile.VOID
                            || rc.senseTerrainTile(next_square) == TerrainTile.OFF_MAP) {
                        last_wall = next_square;
                    }
                    else {
                        break;
                    }
                }
                Direction toNextSquare = rc.getLocation().directionTo(next_square);
                if (toNextSquare.opposite().equals(toTarget)) {
                    direction_to_turn *= -1;
                    navigateTo(rc, target);
                    return;
                }
                if (rc.canMove(toNextSquare)) {
                    rc.sneak(toNextSquare);
                    return;
                }
                else {
                    // We are being blocked by our teammate
                    Direction moveDirection = directions[(toNextSquare.ordinal()
                            + direction_to_turn + 8) % 8];
                    if (rc.canMove(moveDirection)) {
                        bugging = false;
                        rc.sneak(moveDirection);
                        return;
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Soldier Exception");
        }
    }
}
