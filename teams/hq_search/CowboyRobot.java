package hq_search;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.TerrainTile;

public class CowboyRobot extends BaseRobot {
    static Direction[]     directions = { Direction.NORTH, Direction.NORTH_EAST, Direction.EAST,
            Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST,
            Direction.NORTH_WEST     };
    public TerrainTile[][] gameBoard;
    public int             coarseness = 2;
    public int[][]         coarseMap  = null;

    public CowboyRobot(RobotController myRC) throws GameActionException {
        super(myRC);
        setupGameBoard();
        setupCoarseMap();
        MapLocation target = rc.senseEnemyHQLocation();
        int startX = target.x / coarseness;
        int startY = target.y / coarseness;
        Dijkstra.setupDijkstra(coarseMap, startX, startY);
        GeneralNavigation.setupNav(rc, coarseness, target);
    }

    public void setupGameBoard() {
        // Scan the grid
        int height = rc.getMapHeight();
        int width = rc.getMapWidth();
        gameBoard = new TerrainTile[width][height];
        for (int y = height - 1; y >= 0; y--) {
            for (int x = width - 1; x >= 0; x--) {
                gameBoard[x][y] = rc.senseTerrainTile(new MapLocation(y, x));
            }
        }
    }

    public void setupCoarseMap() {
        // Create new map for given coarseness
        int height = (int) Math.ceil((double) rc.getMapHeight() / coarseness);
        int width = (int) Math.ceil((double) rc.getMapWidth() / coarseness);
        coarseMap = new int[height][width];
        // Populate the coarseMap
        for (int y = 0; y < rc.getMapHeight(); y++) {
            int coarseY = y / coarseness;
            for (int x = 0; x < rc.getMapWidth(); x++) {
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
    }

    public void run() {
        try {
            MapLocation target = rc.senseEnemyHQLocation();

            // TODO: Dijkstra.finished should be Dijkstra.finished || RPC.loaded
            if (!Dijkstra.finished) {
                // BugNavigator.navigateTo(rc, target);
                // Run a graph search on coarseMap. with the cost of each edge being cost of target
                Dijkstra.doDijkstra();
            }
            else {
                GeneralNavigation.smartNav(rc);
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
