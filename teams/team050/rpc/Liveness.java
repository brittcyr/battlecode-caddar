package team050.rpc;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.RobotType;

public class Liveness {
    // Each round robots post their liveness field to (current round number, RobotType).
    // HQ checks these fields to garbage collect radio state each round.

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
        System.out.println(gid);
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
}
