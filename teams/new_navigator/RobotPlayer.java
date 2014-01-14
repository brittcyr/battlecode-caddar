package new_navigator;

import java.util.Random;

import battlecode.common.RobotController;

public class RobotPlayer {
    static Random rand;

    public static void run(RobotController rc) {
        BaseRobot br = null;
        try {
            switch (rc.getType()) {
                case HQ:
                    br = new HQRobot(rc);
                    break;
                case SOLDIER:
                    br = new CowboyRobot(rc);
                    break;
                case PASTR:
                    br = new PastrRobot(rc);
                    break;
                case NOISETOWER:
                    br = new NoiseTowerRobot(rc);
                    break;
                default:
                    throw new Exception("FAILED TO INITIALIZE");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // Main loop should never terminate
        while (true) {
            try {
                br.loop();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
