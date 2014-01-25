package new_navigator_clans;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.TerrainTile;

public class GeneralNavigation {
    public static Direction[]     directions = { Direction.NORTH, Direction.NORTH_EAST,
            Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST,
            Direction.WEST, Direction.NORTH_WEST };
    public static MapLocation     target;
    public static MapLocation     lastWaypoint;
    public static int             coarseness = 2;
    public static TerrainTile[][] gameBoard;
    public static int[][]         coarseMap  = null;

    public static void setupNav(RobotController rc) {
        senseGameBoard(rc);

        // We will only need a coarse map if we compute ourself hopefully will not have to
        lastWaypoint = null;
    }

    public static void setTarget(MapLocation _target) {
        target = _target;
    }

    public static int detectMyCoarseX(RobotController rc) {
        MapLocation myLoc = rc.getLocation();
        int myX = myLoc.x;
        return myX / coarseness;
    }

    public static int detectMyCoarseY(RobotController rc) {
        MapLocation myLoc = rc.getLocation();
        int myY = myLoc.y;
        return myY / coarseness;
    }

    public static MapLocation getMyCenter(RobotController rc) {
        int coarseX = detectMyCoarseX(rc);
        int coarseY = detectMyCoarseY(rc);
        int fineX = coarseX * coarseness + coarseness / 2;
        int fineY = coarseY * coarseness + coarseness / 2;
        return new MapLocation(fineX, fineY);
    }

    public static MapLocation getNextCenter(RobotController rc, int coarseness, Direction d) {
        MapLocation myCenter = getMyCenter(rc);
        return myCenter.add(d, coarseness);
    }

    public static void smartNav(RobotController rc) {
        // Do smart navigation to enemy
        int coarseX = GeneralNavigation.detectMyCoarseX(rc);
        int coarseY = GeneralNavigation.detectMyCoarseY(rc);
        int directionNum = Dijkstra.previous[coarseY][coarseX];

        // This means that we are close to the target and should just use bug
        if (directionNum == Dijkstra.UNSET) {
            BugNavigator.navigateTo(rc, target);
            return;
        }

        Direction toWaypoint = directions[directionNum];
        MapLocation waypoint = GeneralNavigation.getNextCenter(rc, coarseness, toWaypoint);
        if (!waypoint.equals(lastWaypoint)) {
            BugNavigator.bugReset();
            lastWaypoint = waypoint;
        }

        BugNavigator.navigateTo(rc, waypoint);
    }

    public static void senseGameBoard(RobotController rc) {
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

    public static void setupCoarseMap(RobotController rc) {
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
                if (tile == TerrainTile.NORMAL) {
                    coarseMap[coarseY][coarseX] += 10;
                }
                else {
                    if (tile == TerrainTile.ROAD) {
                        coarseMap[coarseY][coarseX] += 7;
                    }
                    else {
                        // Then it must be a void
                        coarseMap[coarseY][coarseX] += 1000;
                    }
                }
            }
        }
        Dijkstra.setupDijkstra(coarseMap, target.x / coarseness, target.y / coarseness);
    }

}
