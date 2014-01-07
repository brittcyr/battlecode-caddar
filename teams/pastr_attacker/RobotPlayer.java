package pastr_attacker;

import battlecode.common.RobotController;

public class RobotPlayer {

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
        }

        // Main loop should never terminate
        while (true) {
            try {
                br.loop();
            }
            catch (Exception e) {
                System.out.println("Main loop terminated unexpectedly");
            }
        }
    }
}
