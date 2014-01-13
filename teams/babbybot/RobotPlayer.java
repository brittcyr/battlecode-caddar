package babbybot;

import battlecode.common.RobotController;

public class RobotPlayer {

    private static RobotController rc;

    public static void run(RobotController rc) {
        RobotPlayer.rc = rc;
        switch (rc.getType()) {
            case HQ:
                handleHq();
                break;
            case SOLDIER:
                handleSoldier();
                break;
            case NOISETOWER:
                handleNoiseTower();
                break;
            case PASTR:
                handlePastr();
                break;
            default:
                return;

        }
    }

    private static void handleNoiseTower() {
        while (true) {
            rc.yield();
        }
    }

    private static void handlePastr() {
        while (true) {
            rc.yield();
        }
    }

    private static void handleSoldier() {
        while (true) {
            rc.yield();
        }
    }

    private static void handleHq() {
        while (true) {
            rc.yield();
        }
    }
}
