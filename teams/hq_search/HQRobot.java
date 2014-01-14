package hq_search;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.TerrainTile;

public class HQRobot extends BaseRobot {
    public TerrainTile[][] gameBoard;
    public int[][]         coarseMap = null;

    public HQRobot(RobotController myRC) throws GameActionException {
        super(myRC);
        int width = myRC.getMapWidth();
        int height = myRC.getMapHeight();
        gameBoard = new TerrainTile[width][height];
    }

    public void run() {
        try {
            boolean doSpawn = false;
            Direction toEnemy = null;
            // Check if a robot is spawnable and spawn one if it is
            if (rc.isActive() && rc.senseRobotCount() < 1) {// GameConstants.MAX_ROBOTS) {
                toEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
                if (rc.senseObjectAtLocation(rc.getLocation().add(toEnemy)) == null) {
                    doSpawn = true;
                }
            }

            if (doSpawn) {
                rc.spawn(toEnemy);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("HQ Exception");
        }
    }
}
