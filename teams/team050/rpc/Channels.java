package team050.rpc;

/*
 * Treat radio channels like a big address space. Each channel can store an int (32 bits) of data.
 * We have 2^16 = 0x10000 = 65536 channels. Then each channel addresses a single 32-bit word.
 * 
 *      +-------------------+
 *      |  (32 bits)        |
 *      +-------------------+  <- Channel 65536
 *      |                   |
 *      |                   |
 *      \/\/\/\/\/\/\/\/\/\/\
 *      
 *      
 *      /\/\/\/\/\/\/\/\/\/\/
 *      |                   |
 *      +-------------------+
 *      |                   |
 *      |  Clan             |
 *      |  Address Space    |
 *      |                   |
 *      |                   |
 *      +-------------------+  <- CLAN_ADDR_SPACE_START
 *      |                   |
 *      \/\/\/\/\/\/\/\/\/\/\
 *      
 *      
 *      /\/\/\/\/\/\/\/\/\/\/
 *      |                   |
 *      |                   |
 *      +-------------------+
 *      |                   |
 *      +-------------------+  <- Channel 0x00000
 *      
 * This class stores common pointers into this "address space."
 */

public class Channels {

    // Used to map large game element IDs (e.g. rc.getRobot().getId()) to smaller addr space.
    public static final int MAX_GAME_OBJS         = 1009;                            // 1009 prime.
    public static final int MAX_CLANS             = 20;

    // We store the radio "address" at which each section starts, as well as the size of that
    // section (*_SZ). Then functions can index into that section to retrieve the information they
    // seek. Think of each section as an array of values, and *_SZ is the length of that array. For
    // example, CLAN_MEMBERSHIPS is an array of length CLAN_MEMBERSHIPS_SZ that stores membership
    // information.
    
    // Clan-mode specific variables.
    public static final int BUILDER_PASTR_EXISTS_OFFSET = 0;
    public static final int BUILDER_PASTR_EXISTS_SZ = 1;
    public static final int BUILDER_NT_EXISTS_OFFSET = 1;
    public static final int BUILDER_NT_EXISTS_SZ = 1;

    // Clan information.
    public static final int CLAN_ADDR_SPACE_START = 50000;
    public static final int CLAN_PRIV_MEM_SZ = 5;

    // The total number of clans.
    public static final int NUM_CLANS             = CLAN_ADDR_SPACE_START;
    public static final int NUM_CLANS_SZ          = 1;

    // What clan each robot is in.
    public static final int CLAN_MEMBERSHIPS      = CLAN_ADDR_SPACE_START + NUM_CLANS_SZ;
    public static final int CLAN_MEMBERSHIPS_SZ   = MAX_GAME_OBJS;

    // The size of each clan.
    public static final int CLAN_SIZES            = CLAN_MEMBERSHIPS + CLAN_MEMBERSHIPS_SZ;
    public static final int CLAN_SIZES_SZ         = MAX_CLANS;

    // Each clan's waypoint.
    public static final int CLAN_WAYPOINTS        = CLAN_SIZES + CLAN_SIZES_SZ;
    public static final int CLAN_WAYPOINTS_SZ     = MAX_CLANS;

    // Each clan's behavior mode.
    public static final int CLAN_MODES            = CLAN_WAYPOINTS + CLAN_WAYPOINTS_SZ;
    public static final int CLAN_MODES_SZ         = MAX_CLANS;
    
    // Clan-private memory.
    public static final int CLAN_MEM = CLAN_MODES + CLAN_MODES_SZ;
    public static final int CLAN_MEM_SZ = MAX_CLANS * CLAN_PRIV_MEM_SZ;
}
