package doNothing;

import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;

public class RobotPlayer {

    public static void run(RobotController rc) {

        while (true) {
            if (rc.getType() == RobotType.HQ) {
                try {
                    // Check if a robot is spawnable and spawn one if it is
                    if (rc.isActive() && rc.senseRobotCount() < 25) {
                        Direction toEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
                        if (rc.senseObjectAtLocation(rc.getLocation().add(toEnemy)) == null) {
                            rc.spawn(toEnemy);
                        }
                    }
                }
                catch (Exception e) {
                    System.out.println("HQ Exception");
                }
            }

            rc.yield();
        }
    }
}
