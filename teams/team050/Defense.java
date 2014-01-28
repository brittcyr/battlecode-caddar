package team050;

import team050.rpc.Clans;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.TerrainTile;

public class Defense {
	public final static int DIST_FROM_PASTR = 7;

    public static Direction[] directions = { Direction.NORTH, Direction.NORTH_EAST, Direction.EAST,
            Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST,
            Direction.NORTH_WEST        };

    // Whether we are running in or sneaking out
    public static boolean     goingIn    = false;

    // How far we are able to walk in each dir
    public static int[]       distInDir  = null;

    // What we are defending
    public static MapLocation pastr;

    // Current direction
    public static int         direction  = 4;

    // Initialize how far we can go in each direction
    public static void initDirs(RobotController rc) throws GameActionException {
        pastr = Clans.getWaypoint(CowboyRobot.clan);
        if (distInDir != null) {
            return;
        }
        distInDir = new int[8];
        for (int dir = 0; dir < 8; dir++) {
            int range = 0;
            for (; ++range < DIST_FROM_PASTR;) {
                TerrainTile terrain = rc.senseTerrainTile(pastr.add(directions[dir], range));
                if (terrain == TerrainTile.VOID || terrain == TerrainTile.OFF_MAP) {
                    range--;
                    break;
                }
            }
            distInDir[dir] = range;
        }
    }

    public static void doDefense(RobotController rc) throws GameActionException {
        if (goingIn) {
            // Check if we have push cows into the pastr
            if (rc.getLocation().distanceSquaredTo(pastr) > 7) {
                rc.move(BugNavigator.getDirectionTo(rc, pastr));
                return;
            }

            // Otherwise, we have push in and need to go out
            goingIn = false;
            BugNavigator.bugReset();
            direction += 5;
            direction %= 8;
            while (distInDir[direction] < 4) {
                direction += 5;
                direction %= 8;
            }
        }

        if (!goingIn) {
            if (rc.getLocation().distanceSquaredTo(pastr) > 
            Math.min(DIST_FROM_PASTR * DIST_FROM_PASTR, distInDir[direction] * distInDir[direction])) {
                goingIn = true;
                BugNavigator.bugReset();
                doDefense(rc);
                return;
            }

            // Otherwise we are still going out
            rc.sneak(BugNavigator.getDirectionTo(rc,
                    pastr.add(directions[direction], Math.min(DIST_FROM_PASTR, distInDir[direction]))));
        }

    }
}
