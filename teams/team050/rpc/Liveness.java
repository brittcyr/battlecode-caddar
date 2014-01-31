package team050.rpc;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.RobotType;

public class Liveness {
    // Each round robots post their liveness field to (current round number, RobotType).
    // HQ checks these fields to garbage collect radio state each round.

    // Bots expected to update liveness at worst every 10 rounds.
    public static final int LIVENESS_UPDATE_PERIOD = 20;

    public static int roundAndTypeToInt(int roundNum, RobotType type) {
        return roundNum + ((GameConstants.ROUND_MAX_LIMIT + 1) * type.ordinal());
    }

    // Retrieve round from stored word.
    public static int wordToRound(int word) {
        return word % (GameConstants.ROUND_MAX_LIMIT + 1);
    }

    // Retrieve type from stored word.
    public static RobotType wordToType(int word) {
        int x = word / (GameConstants.ROUND_MAX_LIMIT + 1);
        return RobotType.values()[x];
    }

    // Clear liveness slot.
    public static void clearLiveness(int gid) throws GameActionException {
        Radio.putData(Channels.BOT_LIVENESS + gid, new int[] { 0 });
    }

    // Update a robot's liveness.
    public static void updateLiveness(RobotType type, int gid) throws GameActionException {
        int roundNum = Clock.getRoundNum();
        int word = roundAndTypeToInt(roundNum, type);
        Radio.putData(Channels.BOT_LIVENESS + gid, new int[] { word });
    }

    // Last round that robot posted update.
    public static int getLastPostedRoundByGid(int gid) throws GameActionException {
        assert (gid < Channels.MAX_ROBOTS);
        int word = Radio.getData(Channels.BOT_LIVENESS + gid, 1)[0];
        return Liveness.wordToRound(word);
    }

    // Last round that robot posted update.
    public static RobotType getLastPostedType(int gid) throws GameActionException {
        assert (gid < Channels.MAX_ROBOTS);
        int word = Radio.getData(Channels.BOT_LIVENESS + gid, 1)[0];
        return Liveness.wordToType(word);
    }

    /*
     * Used for debugging.
     */
    public static void printLivenessTable() throws GameActionException {
        int[] livenessTable = Radio.getData(Channels.BOT_LIVENESS, Channels.BOT_LIVENESS_SZ);
        assert (livenessTable.length == Channels.BOT_LIVENESS_SZ);
        System.out.println();
        System.out.printf("\n\n\n\n*** ROUND %d ***\n", Clock.getRoundNum());
        for (int i = 0; i < livenessTable.length; i++) {
            if (i % 10 == 0) {
                System.out.printf("========== CLAN %d (%s) ===========\n", i / 10,
                        Clans.getClanMode(i / 10));
                System.out.printf("========== SIZE %d        ===========\n",
                        Clans.getClanSize(i / 10));
                System.out.printf("========== PSTR %b    ===========\n",
                        Clans.getClanPastrStatus(i / 10));
                System.out.printf("========== NT   %b    ===========\n",
                        Clans.getClanNTStatus(i / 10));
            }
            System.out.printf("GID %d:\t[TYPE: %s][ROUND: %d]\n", i, wordToType(livenessTable[i]),
                    wordToRound(livenessTable[i]));
        }
    }
}
