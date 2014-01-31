package team050;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
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

    /*
     * Reset bugging so that we can use this if targets
     */
    public static void bugReset() {
        bugging = false;
    }

    /*
     * Sneak towards the target
     */
    @Deprecated
    public static void navigateTo(RobotController rc, MapLocation target)
            throws GameActionException {
        rc.sneak(getDirectionTo(rc, target));
    }

    /*
     * Determine the direction to get to the target. This will always tell you a direction that it
     * thinks will eventually get you closer using the bugging algorithm
     */
    public static Direction getDirectionTo(RobotController rc, MapLocation target) {
        try {
            turned &= bugging;
            MapLocation myLoc = rc.getLocation();
            Direction toTarget = myLoc.directionTo(target);
            // If we are free from a wall
            if (!bugging) {
                if (rc.canMove(toTarget)) {
                    // If we can get there on a road, do that
                    MapLocation next = myLoc.add(toTarget);
                    if (rc.senseTerrainTile(next) == TerrainTile.ROAD) {
                        return toTarget;
                    }

                    // First check the deltaX and deltaY to the target
                    MapLocation nextSquare = myLoc.add(toTarget);
                    int deltaX = Math.abs(nextSquare.x - target.x);
                    int deltaY = Math.abs(nextSquare.y - target.y);
                    int bigger = Math.max(deltaX, deltaY);

                    // Try left
                    Direction left = toTarget.rotateLeft();
                    if (rc.canMove(left)) {
                        MapLocation leftSquare = myLoc.add(left);
                        int leftDeltaX = Math.abs(leftSquare.x - target.x);
                        int leftDeltaY = Math.abs(leftSquare.y - target.y);
                        int biggerLeft = Math.max(leftDeltaX, leftDeltaY);
                        boolean isRoad = rc.senseTerrainTile(leftSquare) == TerrainTile.ROAD;
                        if (isRoad && biggerLeft == bigger) {
                            return left;
                        }
                    }

                    // Try right
                    Direction right = toTarget.rotateRight();
                    if (rc.canMove(right)) {
                        MapLocation rightSquare = myLoc.add(right);
                        int rightDeltaX = Math.abs(rightSquare.x - target.x);
                        int rightDeltaY = Math.abs(rightSquare.y - target.y);
                        int biggerRight = Math.max(rightDeltaX, rightDeltaY);
                        boolean isRoad = rc.senseTerrainTile(rightSquare) == TerrainTile.ROAD;
                        if (isRoad && biggerRight == bigger) {
                            return right;
                        }
                    }

                    // Just follow the normal
                    return toTarget;
                }
                last_wall = myLoc.add(toTarget);
                dist_to_target_at_bug_start = myLoc.distanceSquaredTo(target);

                // Determine which direction we should try to bug around
                double dist_plus = 9999;
                double dist_minus = 9999;
                Direction dir = toTarget;
                for (int x = 1; x < 4; x++) {
                    dir = dir.rotateRight();
                    MapLocation possible = myLoc.add(dir);
                    TerrainTile tile = rc.senseTerrainTile(possible);
                    if (tile == TerrainTile.NORMAL || tile == TerrainTile.ROAD) {
                        dist_plus = possible.distanceSquaredTo(target);
                        break;
                    }
                }
                dir = toTarget;
                for (int x = 1; x < 4; x++) {
                    dir = dir.rotateLeft();
                    MapLocation possible = myLoc.add(dir);
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
                if (myLoc.distanceSquaredTo(target) <= dist_to_target_at_bug_start) {
                    if (rc.canMove(toTarget)) {
                        bugging = false;
                        // Call it again with navigating not in bugging mode
                        return getDirectionTo(rc, target);
                    }
                }

                // Take the direction to wall and add one until valid terrain, then move that way
                MapLocation next_square = null;
                for (int x = 1; x < 8; x++) {
                    Direction to_check = myLoc.directionTo(last_wall);
                    next_square = myLoc
                            .add(directions[(to_check.ordinal() + 1 * direction_to_turn + 8) % 8]);
                    TerrainTile nextTerrain = rc.senseTerrainTile(next_square);
                    if (nextTerrain == TerrainTile.VOID || nextTerrain == TerrainTile.OFF_MAP) {
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
                Direction toNextSquare = myLoc.directionTo(next_square);
                if (toNextSquare.opposite().equals(toTarget) && !turned) {
                    MapLocation possibleNextWall = last_wall;
                    boolean willGetOut = false;

                    for (int x = 0; x < 5; x++) {
                        possibleNextWall = possibleNextWall.add(toNextSquare);
                        TerrainTile possibleWallTerrain = rc.senseTerrainTile(possibleNextWall);
                        if (possibleWallTerrain == TerrainTile.ROAD
                                || possibleWallTerrain == TerrainTile.NORMAL) {
                            willGetOut = true;
                        }
                    }
                    if (!willGetOut) {
                        direction_to_turn *= -1;
                        turned = true;
                        return getDirectionTo(rc, target);
                    }
                }
                if (rc.canMove(toNextSquare)) {
                    return toNextSquare;
                }
                else {
                    Direction moveDirection = directions[(toNextSquare.ordinal()
                            + direction_to_turn + 8) % 8];
                    if (rc.canMove(moveDirection)) {
                        bugReset();
                        return moveDirection;
                    }
                    // Check if we can go perpendicular around
                    moveDirection = directions[(toNextSquare.ordinal() + 2 * direction_to_turn + 8) % 8];
                    if (rc.canMove(moveDirection)) {
                        bugReset();
                        return moveDirection;
                    }
                    else {
                        // Otherwise we will go other way
                        direction_to_turn *= -1;
                        turned = true;
                        return getDirectionTo(rc, target);
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Bugging Exception");
        }
        return Direction.NONE;
    }
}
