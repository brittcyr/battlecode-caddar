package team050.rpc;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
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

    // Update a robot's liveness.
    // TODO: Could check for collisions here (if data in BOT_LIVENESS + GID) has already been
    // written this round.
    public static void updateLiveness(RobotController rc) throws GameActionException {
        int roundNum = Clock.getRoundNum();
        RobotType type = rc.getType();
        int word = roundAndTypeToInt(roundNum, type);

        int gid = rc.getRobot().getID() % Channels.MAX_GAME_OBJS;
        Radio.putData(Channels.BOT_LIVENESS + gid, new int[] { word });
    }

    // Last round that robot posted update.
    public static int getLastPostedRoundByGid(int gid) throws GameActionException {
        assert (gid < Channels.MAX_GAME_OBJS);
        int word = Radio.getData(Channels.BOT_LIVENESS + gid, 1)[0];
        return Liveness.wordToRound(word);
    }

    // Most recently posted type of robot.
    public static RobotType getLastPostedTypeByGid(int gid) throws GameActionException {
        assert (gid < Channels.MAX_GAME_OBJS);
        int word = Radio.getData(Channels.BOT_LIVENESS + gid, 1)[0];
        return Liveness.wordToType(word);
    }
}
