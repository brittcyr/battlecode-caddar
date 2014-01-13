package rpcb.rpc;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

/*
 * When a cowboy is born, it must be assigned to a clan. This RPC library enables access to shared
 * information about clans.
 */
public class Clans {

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

}
