package bug_bot.rpc;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class Radio {

    public static RobotController rc;

    public static void setRobotController(RobotController rc) {
        Radio.rc = rc;
    }

    /*
     * Retrieve data from radio.
     */
    public static int[] getData(int channel, int sz) throws GameActionException {
        int[] data = new int[sz];
        for (int i = 0; i < sz; i++) {
            data[i] = rc.readBroadcast(channel + i);
        }
        return data;
    }

    /*
     * Write data to radio.
     */
    public static void putData(int channel, int[] data) throws GameActionException {
        for (int i = 0; i < data.length; i++) {
            rc.broadcast(channel + i, data[i]);
        }
    }

}
