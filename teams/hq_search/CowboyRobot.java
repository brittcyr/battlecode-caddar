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
    public int             coarseness = 3;
    public int[][]         coarseMap  = null;
    public boolean         done       = false;

    public CowboyRobot(RobotController myRC) throws GameActionException {
        super(myRC);
        setupGameBoard();
        setupCoarseMap();
    }

    public void setupGameBoard() {
        // Scan the grid
        int height = rc.getMapHeight();
        int width = rc.getMapWidth();
        int scanRow = 0;
        gameBoard = new TerrainTile[width][height];
        while (scanRow < height) {
            for (int x = 0; x < width; x++) {
                gameBoard[x][scanRow] = rc.senseTerrainTile(new MapLocation(scanRow, x));
            }
            scanRow++;
        }
    }

    public void setupCoarseMap() {
        // Create new map for given coarseness
        if (coarseMap == null) {
            int height = (int) Math.ceil((double) rc.getMapHeight() / coarseness);
            int width = (int) Math.ceil((double) rc.getMapWidth() / coarseness);
            coarseMap = new int[height][width];
            for (int x = 0; x < height; x++) {
                for (int y = 0; y < width; y++) {
                    coarseMap[x][y] = 0;
                }
            }
            // Populate the coarseMap
            for (int x = 0; x < rc.getMapHeight(); x++) {
                for (int y = 0; y < rc.getMapWidth(); y++) {
                    if (gameBoard[x][y] == TerrainTile.VOID) {
                        coarseMap[x / coarseness][y / coarseness] += 1000;
                    }
                    if (gameBoard[x][y] == TerrainTile.ROAD) {
                        coarseMap[x / coarseness][y / coarseness] += 7;
                    }
                    if (gameBoard[x][y] == TerrainTile.NORMAL) {
                        coarseMap[x / coarseness][y / coarseness] += 10;
                    }
                }
            }
            int startX = rc.senseEnemyHQLocation().x / coarseness;
            int startY = rc.senseEnemyHQLocation().y / coarseness;
            Dijkstra.setupDijkstra(coarseMap, startX, startY);
        }
    }

    public void run() {
        try {

            // Run a graph search on coarseMap. with the cost of each edge being cost of target
            if (!done) {
                Dijkstra.doDijkstra();
                done = Dijkstra.finished;
            }

            if (done) {
                // TODO: Fix the setup nav
                GeneralNavigation.setupNav(rc, coarseness, rc.senseEnemyHQLocation());
                GeneralNavigation.smartNav(rc);
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
