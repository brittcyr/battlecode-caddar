package team050;

import team050.Dijkstra;
import team050.rpc.CoopNav;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.TerrainTile;

public class GeneralNavigation {
    public static Direction[]     directions      = { Direction.NORTH, Direction.NORTH_EAST,
            Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST,
            Direction.WEST, Direction.NORTH_WEST };
    public static MapLocation     target          = null;
    public static MapLocation     prevWaypoint    = null;
    public static int             coarseness      = 2;
    public static TerrainTile[][] gameBoard       = null;
    public static int[][]         coarseMap       = null;
    public static MapLocation     currentlyLoaded = null;
    public static int[][]         previous        = null;

    public static void setTarget(MapLocation _target) {
        target = _target;
    }

    private static int detectMyCoarseX(MapLocation myLoc) {
        int myX = myLoc.x;
        return myX / coarseness;
    }

    private static int detectMyCoarseY(MapLocation myLoc) {
        int myY = myLoc.y;
        return myY / coarseness;
    }

    private static MapLocation getMyCenter(RobotController rc) {
        MapLocation myLoc = rc.getLocation();
        int coarseX = detectMyCoarseX(myLoc);
        int coarseY = detectMyCoarseY(myLoc);
        int fineX = coarseX * coarseness + coarseness / 2;
        int fineY = coarseY * coarseness + coarseness / 2;
        return new MapLocation(fineX, fineY);
    }

    private static MapLocation getNextCenter(RobotController rc, int coarseness, Direction d) {
        MapLocation myCenter = getMyCenter(rc);
        return myCenter.add(d, coarseness);
    }

    private static void senseGameBoard(RobotController rc) {
        // Scan the grid
        int height = rc.getMapHeight();
        int width = rc.getMapWidth();
        gameBoard = new TerrainTile[height][width];
        for (int y = width - 1; y >= 0; y--) {
            for (int x = height - 1; x >= 0; x--) {
                gameBoard[x][y] = rc.senseTerrainTile(new MapLocation(y, x));
            }
        }
    }

    private static void setupCoarseMap(RobotController rc) {
        // Create new map for given coarseness
        int height = (int) Math.ceil((double) rc.getMapHeight() / coarseness);
        int width = (int) Math.ceil((double) rc.getMapWidth() / coarseness);
        coarseMap = new int[height][width];
        // Populate the coarseMap
        int mapHeight = rc.getMapHeight();
        int mapWidth = rc.getMapWidth();
        for (int y = mapHeight; --y >= 0;) {
            int coarseY = y / coarseness;
            for (int x = mapWidth; --x >= 0;) {
                int coarseX = x / coarseness;
                TerrainTile tile = gameBoard[y][x];
                coarseMap[coarseY][coarseX] += getTileValue(tile);
            }
        }
    }

    private static int getTileValue(TerrainTile tile) {
        switch (tile) {
        // A normal square takes 2 turns of action delay wait
            case NORMAL:
                return 2;
                // On a road, there is 1 action delay
            case ROAD:
                return 1;
            case VOID:
            case OFF_MAP:
                return 100;
        }
        return 100;
    }

    public static void prepareCompute(RobotController rc, MapLocation target) {
        // This is the test if we are preparing our first computation
        if (gameBoard == null) {
            senseGameBoard(rc);
            setupCoarseMap(rc);
        }

        int startX = target.x / coarseness;
        int startY = target.y / coarseness;
        Dijkstra.setupDijkstra(coarseMap, startX, startY);
    }

    public static void doCompute() {
        Dijkstra.doDijkstra();
        previous = Dijkstra.previous;
    }

    public static Direction getNextDirection(RobotController rc) throws GameActionException {
        MapLocation myLoc = rc.getLocation();
        // Check if we are ready to use big navigation or if we have to use bug nav
        if (!CoopNav.isComputationReady(target)) {
            CoopNav.requestComputation(target, coarseness);
            return BugNavigator.getDirectionTo(rc, target);
        }

        // Do smart navigation to enemy
        int coarseX = GeneralNavigation.detectMyCoarseX(myLoc);
        int coarseY = GeneralNavigation.detectMyCoarseY(myLoc);
        int directionNum = CoopNav.getDirectionFromResult(target, coarseX, coarseY);

        // This means that we are close to the target or in a bad area and should just use bug
        if (directionNum == Dijkstra.UNSET) {
            return BugNavigator.getDirectionTo(rc, target);
        }

        // If we have at least one more direction to go, then BugNavigate to waypoint
        Direction toWaypoint = directions[directionNum];
        MapLocation waypoint = GeneralNavigation.getNextCenter(rc, coarseness, toWaypoint);
        if (!waypoint.equals(prevWaypoint)) {
            BugNavigator.bugReset();
            prevWaypoint = waypoint;
        }

        return BugNavigator.getDirectionTo(rc, waypoint);
    }
}
