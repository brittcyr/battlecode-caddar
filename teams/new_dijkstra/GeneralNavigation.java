package new_dijkstra;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class GeneralNavigation {
    static Direction[] directions = { Direction.NORTH, Direction.NORTH_EAST, Direction.EAST,
            Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST,
            Direction.NORTH_WEST };
    static MapLocation target;
    static MapLocation lastWaypoint;
    static int         coarseness;

    public static void setupNav(RobotController rc, int _coarseness, MapLocation _target) {
        target = _target;
        coarseness = _coarseness;
        lastWaypoint = null;
        // TODO: Use the radio to determine if Dijkstra is available for our target
    }

    private static int detectMyCoarseX(RobotController rc) {
        MapLocation myLoc = rc.getLocation();
        int myX = myLoc.x;
        return myX / coarseness;
    }

    private static int detectMyCoarseY(RobotController rc) {
        MapLocation myLoc = rc.getLocation();
        int myY = myLoc.y;
        return myY / coarseness;
    }

    private static MapLocation getMyCenter(RobotController rc) {
        int coarseX = detectMyCoarseX(rc);
        int coarseY = detectMyCoarseY(rc);
        int fineX = coarseX * coarseness + coarseness / 2;
        int fineY = coarseY * coarseness + coarseness / 2;
        return new MapLocation(fineX, fineY);
    }

    private static MapLocation getNextCenter(RobotController rc, int coarseness, Direction d) {
        MapLocation myCenter = getMyCenter(rc);
        return myCenter.add(d, coarseness);
    }

    public static void smartNav(RobotController rc) {
        // Do smart navigation to enemy
        int coarseX = GeneralNavigation.detectMyCoarseX(rc);
        int coarseY = GeneralNavigation.detectMyCoarseY(rc);
        // TODO: get this from something local instead of Dijkstra.previous
        int directionNum = Dijkstra.previous[coarseY][coarseX];

        // This means that we are close to the target and should just use bug
        if (directionNum == Dijkstra.UNSET) {
            BugNavigator.navigateTo(rc, target);
            return;
        }

        // If we have at least one more direction to go, then BugNavigate to waypoint
        Direction toWaypoint = directions[directionNum];
        MapLocation waypoint = GeneralNavigation.getNextCenter(rc, coarseness, toWaypoint);
        if (!waypoint.equals(lastWaypoint)) {
            BugNavigator.bugReset();
            lastWaypoint = waypoint;
        }
        BugNavigator.navigateTo(rc, waypoint);
    }

}
