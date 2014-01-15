package new_navigator_clans;

import new_navigator_clans.rpc.Clans;
import new_navigator_clans.rpc.Clans.ClanMode;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class CowboyRobot extends BaseRobot {
    public int         clan;
    public MapLocation target;

    public CowboyRobot(RobotController myRC) throws GameActionException {
        super(myRC);

        // Join first clan with less than 5 members.
        for (int i = 0; i < Clans.getNumClans() + 1; i++) {
            if (Clans.getSize(i) < 5) {
                Clans.joinClan(rc, i);
                clan = i;
                break;
            }
        }
        target = Clans.getWaypoint(clan);

        GeneralNavigation.setupNav(rc);
        GeneralNavigation.setTarget(target);
    }

    protected void getUpdates() throws GameActionException {
        target = Clans.getWaypoint(clan);
        rc.setIndicatorString(0, "clan: " + clan);
        rc.setIndicatorString(1, "target: " + target);
    }

    protected void updateInternals() {
        // TODO: This is the state update where we will have to do our own computing
        // Dijkstra.setupDijkstra(coarseMap, target.x, target.y);
    }

    protected void doAction() throws GameActionException {
        /*
         * if (Dijkstra.finished) { GeneralNavigation.smartNav(rc); } else { //
         * BugNavigator.navigateTo(rc, target); }
         */
        // pass
        
        /* Move around randomly until Clan not Idle. */
        if (Clans.getClanMode(clan) == ClanMode.IDLE) {
            Direction dir = GeneralNavigation.directions[rand.nextInt(8)];
            if (rc.canMove(dir))
                    rc.move(dir);
        }
        else {
            BugNavigator.navigateTo(rc, target);
        }
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
