package bug_bot;

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
                default:
                    break;
            }
        }
        catch (Exception e) {
            System.out.println("Robot constructor failed");
            e.printStackTrace();
            br.rc.addMatchObservation(e.toString());
        }

        // Main loop should never terminate
        while (true) {
            try {
                br.loop();
            }
            catch (Exception e) {
                System.out.println("Main loop terminated unexpectedly");
                e.printStackTrace();
                br.rc.addMatchObservation(e.toString());
            }
        }
    }
}
