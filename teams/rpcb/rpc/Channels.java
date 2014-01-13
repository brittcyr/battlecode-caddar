package rpcb.rpc;

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

    // Clan information.
    public static final int CLAN_ADDR_SPACE_START = 60000;
    public static final int NUM_CLANS             = CLAN_ADDR_SPACE_START;
    public static final int CLAN_MEMBERSHIPS      = CLAN_ADDR_SPACE_START + 1;
    public static final int CLAN_SIZES            = CLAN_MEMBERSHIPS + MAX_GAME_OBJS;
    public static final int CLAN_WAYPOINTS        = CLAN_SIZES + MAX_GAME_OBJS;


}
