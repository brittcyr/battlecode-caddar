package hq_search;

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
    static boolean     turned                      = false;

    // TODO: Fix looping and create a bugging reset

    public static void navigateTo(RobotController rc, MapLocation target) {
        try {
            turned &= bugging;
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
                    if (rc.canMove(toTarget)) {
                        bugging = false;
                        // Call it again with navigating not in bugging mode
                        navigateTo(rc, target);
                        return;
                    }
                }

                // Take the direction to wall and add one until valid terrain, then move that way
                MapLocation next_square = null;
                for (int x = 1; x < 8; x++) {
                    Direction to_check = rc.getLocation().directionTo(last_wall);
                    next_square = rc.getLocation().add(
                            directions[(to_check.ordinal() + 1 * direction_to_turn + 8) % 8]);
                    if (rc.senseTerrainTile(next_square) == TerrainTile.VOID
                            || rc.senseTerrainTile(next_square) == TerrainTile.OFF_MAP) {
                        last_wall = next_square;
                    }
                    else {
                        if (rc.senseObjectAtLocation(next_square) != null) {
                            last_wall = next_square;
                        }
                        else {
                            break;
                        }
                    }
                }
                Direction toNextSquare = rc.getLocation().directionTo(next_square);
                if (toNextSquare.opposite().equals(toTarget) && !turned) {
                    // TODO: Check if we have teammates nearby and tell to turn also
                    MapLocation possibleNextWall = last_wall;
                    boolean willGetOut = false;

                    // Check worst case range of sight
                    for (int x = 0; (x + 1) * (x + 1) + 1 < 35; x++) {
                        possibleNextWall = possibleNextWall.add(toNextSquare);
                        if (rc.senseTerrainTile(next_square) == TerrainTile.ROAD
                                || rc.senseTerrainTile(next_square) == TerrainTile.NORMAL) {
                            willGetOut = true;
                        }
                    }
                    if (!willGetOut) {
                        direction_to_turn *= -1;
                        turned = true;
                        navigateTo(rc, target);
                        return;
                    }
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
                        rc.sneak(moveDirection);
                        return;
                    }
                    // Check if we can go perpendicular around
                    moveDirection = directions[(toNextSquare.ordinal() + 2 * direction_to_turn + 8) % 8];
                    if (rc.canMove(moveDirection)) {
                        rc.sneak(moveDirection);
                        return;
                    }
                    else {
                        // Otherwise we will go other way
                        direction_to_turn *= -1;
                        turned = true;
                        navigateTo(rc, target);
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
