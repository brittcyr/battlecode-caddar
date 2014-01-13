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
    public int             scanRow    = 0;
    public int             coarseness = 8;
    public int[][]         coarseMap  = null;
    public boolean         done       = false;

    public CowboyRobot(RobotController myRC) throws GameActionException {
        super(myRC);

        int width = myRC.getMapWidth();
        int height = myRC.getMapHeight();
        gameBoard = new TerrainTile[width][height];

        // Scan the grid
        while (scanRow < rc.getMapHeight()) {
            for (int x = 0; x < rc.getMapWidth(); x++) {
                gameBoard[x][scanRow] = rc.senseTerrainTile(new MapLocation(scanRow, x));
            }
            scanRow++;
        }

    }

    public void run() {
        try {

            // Create new map for given coarseness
            if (coarseMap == null && scanRow == rc.getMapHeight()) {
                coarseMap = new int[(rc.getMapHeight() / coarseness) + 1][(rc.getMapWidth() / coarseness) + 1];
                for (int x = 0; x < (rc.getMapHeight() / coarseness) + 1; x++) {
                    for (int y = 0; y < (rc.getMapWidth() / coarseness) + 1; y++) {
                        coarseMap[x][y] = 3 * coarseness * coarseness;
                    }
                }
                // Populate the coarseMap
                for (int x = 0; x < rc.getMapHeight(); x++) {
                    for (int y = 0; y < rc.getMapWidth(); y++) {
                        if (gameBoard[x][y] == TerrainTile.VOID) {
                            coarseMap[x / coarseness][y / coarseness] += 10;
                        }
                        if (gameBoard[x][y] == TerrainTile.ROAD) {
                            coarseMap[x / coarseness][y / coarseness] -= 1;
                        }
                    }
                }
                int startX = rc.senseEnemyHQLocation().x / coarseness;
                int startY = rc.senseEnemyHQLocation().y / coarseness;
                Dijkstra.setupDijkstra(coarseMap, startX, startY);
            }

            // Run a graph search on coarseMap. with the cost of each edge being cost of target
            if (!done && coarseMap != null) {
                Dijkstra.doDijkstra();
                done = Dijkstra.finished;
            }

            if (done) {
                // Do smart navigation to enemy
                int coarseX = GeneralNavigation.detectMyCoarseX(rc, coarseness);
                int coarseY = GeneralNavigation.detectMyCoarseY(rc, coarseness);
                int directionNum = Dijkstra.previous[coarseY][coarseX];
                if (directionNum == Dijkstra.UNSET) {
                    BugNavigator.navigateTo(rc, rc.senseEnemyHQLocation());
                    return;
                }
                Direction toWaypoint = directions[directionNum];
                MapLocation waypoint = GeneralNavigation.getNextCenter(rc, coarseness, toWaypoint);
                BugNavigator.navigateTo(rc, waypoint);
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
