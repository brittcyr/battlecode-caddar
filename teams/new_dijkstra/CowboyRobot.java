package new_dijkstra;

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
    public static int      coarseness = 2;
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
                coarseMap[coarseY][coarseX] += getTileValue(tile);
            }
        }

        // This pads the last coarse square so that there is no unfair edge bonus
        for (int y = rc.getMapHeight(); y < height * coarseness; y++) {
            int coarseY = y / coarseness;
            for (int x = 0; x < rc.getMapWidth(); x++) {
                int coarseX = x / coarseness;
                TerrainTile tile = gameBoard[rc.getMapHeight() - 1][x];
                coarseMap[coarseY][coarseX] += getTileValue(tile);
            }
        }

        // This pads the last coarse square so that there is no unfair edge bonus
        for (int y = 0; y < rc.getMapHeight(); y++) {
            int coarseY = y / coarseness;
            for (int x = rc.getMapWidth(); x < width * coarseness; x++) {
                int coarseX = x / coarseness;
                TerrainTile tile = gameBoard[y][rc.getMapWidth() - 1];
                coarseMap[coarseY][coarseX] += getTileValue(tile);
            }
        }
    }

    private int getTileValue(TerrainTile tile) {
        switch (tile) {
            case NORMAL:
                return 2;
            case ROAD:
                return 1;
            case VOID:
            case OFF_MAP:
                return 100;
        }
        return 100;
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
