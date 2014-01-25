package new_navigator_clans;

import new_navigator_clans.rpc.Clans;
import new_navigator_clans.rpc.Clans.ClanMode;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import bug_bot.rpc.Channels;

public class HQRobot extends BaseRobot {

    public HQRobot(RobotController myRC) throws GameActionException {
        super(myRC);

        // Initialize clan waypoints.
        MapLocation hq = rc.senseHQLocation();
        for (int clan = 0; clan < Channels.MAX_CLANS; clan++) {
            Clans.setWaypoint(clan, hq);
        }
    }

    protected void getUpdates() {
        // pass
    }

    protected void updateInternals() throws GameActionException {
        // Manage clans.
        for (int i = 0; i < Clans.getNumClans(); i++) {
            System.out.println("Clan " + i);
            System.out.println("\t" + Clans.getClanMode(i));
            switch (Clans.getClanMode(i)) {
                case DEAD:
                    break;
                case IDLE:
                    if (Clans.getSize(i) >= 5) {
                        Clans.setClanMode(i, ClanMode.RAIDER);
                        Clans.setWaypoint(i, rc.senseEnemyHQLocation());
                    }
                    else {
                        Clans.setWaypoint(i, rc.senseHQLocation());
                        break;
                    }
                default:
                    break;
            }
        }
    }

    protected void doAction() throws GameActionException {
        boolean doSpawn = false;
        Direction toEnemy = null;
        // Check if a robot is spawnable and spawn one if it is
        if (rc.isActive() && rc.senseRobotCount() < GameConstants.MAX_ROBOTS) {
            toEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
            if (rc.senseObjectAtLocation(rc.getLocation().add(toEnemy)) == null) {
                doSpawn = true;
            }
        }

        if (doSpawn) {
            rc.spawn(toEnemy);
        }
    }

    protected void sendUpdates() {
        // pass
    }

    protected void doCompute() {
        // pass
    }

}
