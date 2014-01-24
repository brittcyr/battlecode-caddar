package team050.rpc;

/*
 * Treat radio channels like a big address space. Each channel can store an int (32 bits) of data.
 * We have 2^16 = 0x10000 = 65536 channels. Then each channel addresses a single 32-bit word.
 * 
 * This class stores common pointers into this "address space."
 */

public class Channels {

    // Used to map large game element IDs (e.g. rc.getRobot().getId()) to smaller addr space.
    // Actual max game objects is 25. 29 is next largest prime, which is better for open addressing.
    public static final int MAX_GAME_OBJS               = 29;
    public static final int MAX_CLANS                   = 5;

    // An index in [0, MAX_GAME_OBJS) is used to retrieve information about the robot.
    // Call the index gid. We use open addressing to determine the location in the table.

    // We store the radio "address" at which each section starts, as well as the size of that
    // section (*_SZ). Then functions can index into that section to retrieve the information they
    // seek. Think of each section as an array of values, and *_SZ is the length of that array. For
    // example, CLAN_MEMBERSHIPS is an array of length CLAN_MEMBERSHIPS_SZ that stores membership
    // information.

    // Clan-mode specific variables.
    public static final int CLAN_PRIV_MEM_SZ            = 5;
    public static final int BUILDER_PASTR_EXISTS_OFFSET = 0;
    public static final int BUILDER_PASTR_EXISTS_SZ     = 1;
    public static final int BUILDER_NT_EXISTS_OFFSET    = 1;
    public static final int BUILDER_NT_EXISTS_SZ        = 1;

    // Clan information.
    public static final int CLAN_ADDR_SPACE_START       = 40000;

    // Store the rc.getRobot().getID() located in each GID position from [0, MAX_GAME_OBJS). A value
    // of -1 means "vacated" in the context of open addressing.
    public static final int PRESENT_TABLE               = CLAN_ADDR_SPACE_START;
    public static final int PRESENT_TABLE_SZ            = MAX_GAME_OBJS;

    // The total number of clans.
    public static final int NUM_CLANS                   = PRESENT_TABLE + PRESENT_TABLE_SZ;
    public static final int NUM_CLANS_SZ                = 1;

    // What clan each robot is in.
    public static final int CLAN_MEMBERSHIPS            = CLAN_ADDR_SPACE_START + NUM_CLANS_SZ;
    public static final int CLAN_MEMBERSHIPS_SZ         = MAX_GAME_OBJS;

    // Liveness updates for each robot.
    public static final int BOT_LIVENESS                = CLAN_MEMBERSHIPS + CLAN_MEMBERSHIPS_SZ;
    public static final int BOT_LIVENESS_SZ             = MAX_GAME_OBJS;

    // The size of each clan.
    public static final int CLAN_SIZES                  = CLAN_MEMBERSHIPS + CLAN_MEMBERSHIPS_SZ;
    public static final int CLAN_SIZES_SZ               = MAX_CLANS;

    // Each clan's waypoint.
    public static final int CLAN_WAYPOINTS              = CLAN_SIZES + CLAN_SIZES_SZ;
    public static final int CLAN_WAYPOINTS_SZ           = MAX_CLANS;

    // Each clan's behavior mode.
    public static final int CLAN_MODES                  = CLAN_WAYPOINTS + CLAN_WAYPOINTS_SZ;
    public static final int CLAN_MODES_SZ               = MAX_CLANS;

    // Clan-private memory.
    public static final int CLAN_MEM                    = CLAN_MODES + CLAN_MODES_SZ;
    public static final int CLAN_MEM_SZ                 = MAX_CLANS * CLAN_PRIV_MEM_SZ;

}
