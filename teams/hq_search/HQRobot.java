package hq_search;

import java.util.Arrays;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.TerrainTile;

public class HQRobot extends BaseRobot {
    public TerrainTile[][] gameBoard;
    public int             scanRow    = 0;
    public int             coarseness = 16;
    public int[][]         coarseMap  = null;
    public boolean         done       = false;

    public HQRobot(RobotController myRC) throws GameActionException {
        super(myRC);
        int width = myRC.getMapWidth();
        int height = myRC.getMapHeight();
        gameBoard = new TerrainTile[width][height];
        coarseness = 4;
        coarseMap = null;
        done = false;
    }

    public void run() {
        try {
            boolean doSpawn = false;
            Direction toEnemy = null;
            // Check if a robot is spawnable and spawn one if it is
            if (rc.isActive() && rc.senseRobotCount() < 25) {
                toEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
                if (rc.senseObjectAtLocation(rc.getLocation().add(toEnemy)) == null) {
                    doSpawn = true;
                }
            }

            // Scan the grid
            while (scanRow < rc.getMapHeight() && Clock.getBytecodesLeft() > 12 * rc.getMapWidth()) {
                for (int x = 0; x < rc.getMapWidth(); x++) {
                    gameBoard[x][scanRow] = rc.senseTerrainTile(new MapLocation(scanRow, x));
                }
                scanRow++;
                // TODO: Post this to message board so robots can do local pathing
            }

            // Create new map for given coarseness
            if (Clock.getBytecodesLeft() > 100 && coarseMap == null && scanRow == rc.getMapHeight()) {
                coarseMap = new int[(rc.getMapHeight() / coarseness) + 1][(rc.getMapWidth() / coarseness) + 1];
                for (int x = 0; x < (rc.getMapHeight() / coarseness) + 1; x++) {
                    for (int y = 0; y < (rc.getMapWidth() / coarseness) + 1; y++) {
                        // TODO: add a cost here since it is not free to walk on roads
                        coarseMap[x][y] = 0;// coarseness;
                    }
                }
                // Populate the coarseMap
                for (int x = 0; x < rc.getMapHeight(); x++) {
                    for (int y = 0; y < rc.getMapWidth(); y++) {
                        if (gameBoard[x][y] == TerrainTile.VOID) {
                            coarseMap[x / coarseness][y / coarseness] += 1;
                        }
                        // TODO: tweak this to prefer roads
                    }
                }
                for (int[] a : coarseMap) {
                    System.out.println(Arrays.toString(a));
                }
                int startX = rc.senseEnemyHQLocation().x / coarseness;
                int startY = rc.senseEnemyHQLocation().y / coarseness;
                Dijkstra.setupDijkstra(coarseMap, startX, startY);
            }

            // Run a graph search on coarseMap. with the cost of each edge being cost of target
            // TODO: write a graph search to get the best path
            // TODO: make dijkstra able to do across multiple turns
            if (Clock.getBytecodesLeft() > 9500 && !done && coarseMap != null) {
                Dijkstra.doDijkstra();
                done = Dijkstra.finished;
                if (done) {
                    for (int[] a : Dijkstra.previous) {
                        System.out.println(Arrays.toString(a));
                    }
                }
            }

            // Direction[][] resultOfDijkstra = dijkstra(coarseMap, rc.senseHQLocation().x /
            // rc.getMapWidth(), rc.senseHQLocation().y / rc.getMapHeight());

            // TODO: Once we have a best path grid, store it
            // TODO: Respond to requests from robots for the best path to target on macro level
            // TODO: coarseness /= 2;
            // TODO: repeat

            if (doSpawn) {
                rc.spawn(toEnemy);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("HQ Exception");
            rc.suicide();
        }
    }
}
