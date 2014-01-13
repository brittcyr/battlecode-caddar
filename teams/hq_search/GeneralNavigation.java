package hq_search;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class GeneralNavigation {

    public static int detectMyCoarseX(RobotController rc, int coarseness) {
        MapLocation myLoc = rc.getLocation();
        int myX = myLoc.x;
        return myX / coarseness;
    }

    public static int detectMyCoarseY(RobotController rc, int coarseness) {
        MapLocation myLoc = rc.getLocation();
        int myY = myLoc.y;
        return myY / coarseness;
    }

    public static MapLocation getMyCenter(RobotController rc, int coarseness) {
        int coarseX = detectMyCoarseX(rc, coarseness);
        int coarseY = detectMyCoarseY(rc, coarseness);
        int fineX = coarseX * coarseness + coarseness / 2;
        int fineY = coarseY * coarseness + coarseness / 2;
        return new MapLocation(fineX, fineY);
    }

    public static MapLocation getNextCenter(RobotController rc, int coarseness, Direction d) {
        MapLocation myCenter = getMyCenter(rc, coarseness);
        return myCenter.add(d, coarseness);
    }

}
