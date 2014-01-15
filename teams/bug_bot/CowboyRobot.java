package bug_bot;

import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import bug_bot.rpc.Clans;

public class CowboyRobot extends BaseRobot {
    static Random             rand;
    static Direction[]        directions = { Direction.NORTH, Direction.NORTH_EAST, Direction.EAST,
            Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST,
            Direction.NORTH_WEST        };
    public static int         clan       = -1;
    public static MapLocation waypoint;

    public CowboyRobot(RobotController myRC) throws GameActionException {
        super(myRC);
        rand = new Random(myRC.getRobot().getID());

        // Join first clan with less than 5 members.
        for (int i = 0; i < Clans.getNumClans() + 1; i++) {
            if (Clans.getSize(i) < 5) {
                Clans.joinClan(rc, i);
                clan = i;
                break;
            }
        }
        waypoint = Clans.getWaypoint(clan);
    }

    public void run() {
        try {
            // Update radio information.
            waypoint = Clans.getWaypoint(clan);

            System.out.println("CLAN " + clan);
            System.out.println("\twaypoint: " + waypoint);

            // Try to move to the target
            BugNavigator.navigateTo(rc, waypoint);

            return;
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println(e);
            System.out.println("Soldier Exception");
        }
    }
}
