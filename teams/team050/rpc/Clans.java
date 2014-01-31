package team050.rpc;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

/*
 * When a cowboy is born, it must be assigned to a clan. This RPC library enables access to shared
 * information about clans.
 */
public class Clans {

    public static enum ClanMode {
        IDLE, DEFENDER, RAIDER, BUILDER
    }

    public static final ClanMode[] TARGET_CLAN_TYPES = { ClanMode.RAIDER, ClanMode.BUILDER,
            ClanMode.RAIDER, ClanMode.RAIDER        };

    public static int[]            TARGET_CLAN_SIZES = { 5, 5, 5, 30 };

    public static int getClanSize(int clan) throws GameActionException {
        return Radio.getData(Channels.CLAN_SIZES + clan, 1)[0];
    }

    public static void setClanSize(int clan, int size) throws GameActionException {
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

    public static ClanMode getClanMode(int clan) throws GameActionException {
        int modeIndex = Radio.getData(Channels.CLAN_MODES + clan, 1)[0];
        assert (modeIndex >= 0);
        assert (modeIndex < ClanMode.values().length);
        return ClanMode.values()[modeIndex];
    }

    public static void setClanMode(int clan, ClanMode mode) throws GameActionException {
        Radio.putData(Channels.CLAN_MODES + clan, new int[] { mode.ordinal() });
    }

    public static int[] getClanPrivateMemory(int clan, int offset, int len)
            throws GameActionException {
        assert (offset + len <= Channels.CLAN_PRIV_MEM_SZ);
        int channel = Channels.CLAN_MEM + clan * Channels.CLAN_PRIV_MEM_SZ + offset;
        return Radio.getData(channel, len);
    }

    public static void setClanPrivateMemory(int clan, int offset, int[] data)
            throws GameActionException {
        int channel = Channels.CLAN_MEM + clan * Channels.CLAN_PRIV_MEM_SZ + offset;
        Radio.putData(channel, data);
    }

    public static boolean getClanPastrStatus(int clan) throws GameActionException {
        if (Clans.getClanPrivateMemory(clan, Channels.BUILDER_PASTR_EXISTS_OFFSET,
                Channels.BUILDER_PASTR_EXISTS_SZ)[0] == 1) {
            return true;
        }
        else {
            return false;
        }
    }

    public static void setClanPastrStatus(int clan, boolean status) throws GameActionException {
        int val = (status) ? 1 : 0;
        int[] data = { val };
        Clans.setClanPrivateMemory(clan, Channels.BUILDER_PASTR_EXISTS_OFFSET, data);
    }

    public static boolean getClanNTStatus(int clan) throws GameActionException {
        if (Clans.getClanPrivateMemory(clan, Channels.BUILDER_NT_EXISTS_OFFSET,
                Channels.BUILDER_NT_EXISTS_SZ)[0] == 1) {
            return true;
        }
        else {
            return false;
        }
    }

    public static void setClanNTStatus(int clan, boolean status) throws GameActionException {
        int val = (status) ? 1 : 0;
        int[] data = { val };
        Clans.setClanPrivateMemory(clan, Channels.BUILDER_NT_EXISTS_OFFSET, data);
    }
}
