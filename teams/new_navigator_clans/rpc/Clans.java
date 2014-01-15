package new_navigator_clans.rpc;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

/*
 * When a cowboy is born, it must be assigned to a clan. This RPC library enables access to shared
 * information about clans.
 */
public class Clans {

    public static enum ClanMode {
        DEAD, IDLE, DEFENDER, RAIDER, BUILDER
    }

    public static int getNumClans() throws GameActionException {
        return Radio.getData(Channels.NUM_CLANS, 1)[0];
    }

    public static void setNumClans(int numClans) throws GameActionException {
        Radio.putData(Channels.NUM_CLANS, new int[] { numClans });
    }

    public static int getMembership(RobotController rc) throws GameActionException {
        int uid = rc.getRobot().getID() % Channels.MAX_GAME_OBJS;
        return Radio.getData(Channels.CLAN_MEMBERSHIPS + uid, 1)[0];
    }

    public static void setMembership(RobotController rc, int clanId) throws GameActionException {
        int uid = rc.getRobot().getID() % Channels.MAX_GAME_OBJS;
        Radio.putData(Channels.CLAN_MEMBERSHIPS + uid, new int[] { clanId });
    }
    
    public static int getSize(int clan) throws GameActionException {
        return Radio.getData(Channels.CLAN_SIZES + clan, 1)[0];
    }
    
    public static void setSize(int clan, int size) throws GameActionException {
        Radio.putData(Channels.CLAN_SIZES + clan, new int[] { size });
    }

    public static MapLocation getWaypoint(int clan) throws GameActionException {
        int wp = Radio.getData(Channels.CLAN_WAYPOINTS + clan, 1)[0];
        return Marshaler.intToMapLocation(wp);
    }

    public static void setWaypoint(int clan, MapLocation mwp) throws GameActionException {
        int wp = Marshaler.MapLocationToInt(mwp);
        Radio.putData(Channels.CLAN_WAYPOINTS + clan, new int[] { wp });
    }

    // TODO: Use marshaler.
    public static ClanMode getClanMode(int clan) throws GameActionException {
        return ClanMode.values()[Radio.getData(Channels.CLAN_MODES + clan, 1)[0]];
    }
    
    // TODO: Use marshaler.
    public static void setClanMode(int clan, ClanMode mode) throws GameActionException {
        Radio.putData(Channels.CLAN_MODES + clan, new int[] { mode.ordinal() });
    }

    // TODO: Do not exceed MAX_CLANS.
    public static void createClan(int clan) throws GameActionException {
        assert (Clans.getSize(clan) == 0);
        assert (Clans.getClanMode(clan) == ClanMode.DEAD);

        Clans.setClanMode(clan, ClanMode.IDLE);
        Clans.setNumClans(Clans.getNumClans() + 1);
    }

    /*
     * Add rc to clan. If clan does not exist, create it. Set rc to be in clan. Increase the size of
     * clan by one.
     */
    public static void joinClan(RobotController rc, int clan) throws GameActionException {
        if (Clans.getClanMode(clan) == ClanMode.DEAD)
            Clans.createClan(clan);
        Clans.setMembership(rc, clan);
        Clans.setSize(clan, Clans.getSize(clan) + 1);
    }

}
