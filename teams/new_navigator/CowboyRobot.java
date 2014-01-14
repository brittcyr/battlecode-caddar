package new_navigator;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class CowboyRobot extends BaseRobot {
    public MapLocation target;
    static Direction[] directions = { Direction.NORTH, Direction.NORTH_EAST, Direction.EAST,
            Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST,
            Direction.NORTH_WEST };

    public CowboyRobot(RobotController myRC) throws GameActionException {
        super(myRC);
        target = rc.senseEnemyHQLocation();
        GeneralNavigation.setupNav(rc);
        GeneralNavigation.setTarget(target);
    }

    protected void getUpdates() {
        // pass
    }

    protected void updateInternals() {
        // TODO: This is the state update where we will have to do our own computing
        // Dijkstra.setupDijkstra(coarseMap, target.x, target.y);
    }

    protected void doAction() throws GameActionException {
        if (Dijkstra.finished) {
            GeneralNavigation.smartNav(rc);
        }
        else {
            // BugNavigator.navigateTo(rc, target);
        }
        // pass
    }

    protected void sendUpdates() {
        // pass
    }

    protected void doCompute() {
        if (GeneralNavigation.coarseMap == null) {
            GeneralNavigation.setupCoarseMap(rc);
        }

        if (!Dijkstra.finished) {
            Dijkstra.doDijkstra();
        }

        // pass
    }

}
