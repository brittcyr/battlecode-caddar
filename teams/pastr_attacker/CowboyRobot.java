package pastr_attacker;

import java.util.Random;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class CowboyRobot extends BaseRobot {
    static Random   rand;
    private boolean pastrAttacker;

    public CowboyRobot(RobotController myRC) throws GameActionException {
        super(myRC);
        rand = new Random(myRC.getRobot().getID());
        pastrAttacker = false;
    }

    public void run() {
        try {
            // Check if HQ requesting new pastrAttacker.
            // TODO: Use constants for radio channels.
            if (rc.readBroadcast(1001) == 1 && !pastrAttacker) {
                rc.broadcast(1001, 0);
                pastrAttacker = true;
            }
            if (pastrAttacker) {
                rc.broadcast(1000, rc.readBroadcast(1000) + 1); // HQ resets each round.
            }
            if (!rc.isActive()) {
                return;
            }

            // Attack if possible prioritize attacking the weakest enemies first
            // TODO: 10 is magic number.
            Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 10, rc.getTeam()
                    .opponent());
            MapLocation bestAttack = null;
            double leastHealth = 999.9;
            for (int x = 0; x < nearbyEnemies.length; x++) {
                RobotInfo robotInfo = rc.senseRobotInfo(nearbyEnemies[x]);
                if (robotInfo.type == RobotType.SOLDIER) {
                    bestAttack = leastHealth > robotInfo.health ? robotInfo.location : bestAttack;
                    leastHealth = leastHealth > robotInfo.health ? robotInfo.health : leastHealth;
                }
            }
            if (bestAttack != null) {
                rc.attackSquare(bestAttack);
                return;
            }
            for (int x = 0; x < nearbyEnemies.length; x++) {
                RobotInfo robotInfo = rc.senseRobotInfo(nearbyEnemies[x]);
                if (robotInfo.type != RobotType.HQ) {
                    rc.attackSquare(robotInfo.location);
                    return;
                }
            }

            // If a PASTR attacker and enemy has > 0 PASTRs, navigate to PASTR.
            // TODO: Expensive calls to sensePastrLocations()?
            if (pastrAttacker && rc.sensePastrLocations(rc.getTeam().opponent()).length > 0) {
                MapLocation enemyPastrs[] = rc.sensePastrLocations(rc.getTeam().opponent());
                int closestInd = 0;
                int bestDist = 9999;
                for (int i = 0; i < enemyPastrs.length; i++) {
                    if (rc.getLocation().distanceSquaredTo(enemyPastrs[i]) < bestDist) {
                        bestDist = rc.getLocation().distanceSquaredTo(enemyPastrs[i]);
                        closestInd = i;
                    }
                }
                MapLocation pastr = enemyPastrs[closestInd];
                BugNavigator.navigateTo(rc, pastr);
                return;
            }

            // Construct a PASTR immediately if good loc and no enemies.
            int action = (rc.getRobot().getID() * rand.nextInt(101) + 50) % 101;
            if (rc.senseCowsAtLocation(rc.getLocation()) > 2000
                    && rc.senseNearbyGameObjects(Robot.class, 35, rc.getTeam().opponent()).length == 0) {
                rc.construct(RobotType.PASTR);
                return;
            }

            // Construct a PASTR
            if (action < 1
                    && rc.getLocation().distanceSquaredTo(rc.senseHQLocation()) > 20
                    && rc.senseCowGrowth()[rc.getLocation().x][rc.getLocation().y] > 0
                    && rc.senseNearbyGameObjects(Robot.class, 35, rc.getTeam().opponent()).length == 0
                    && rc.senseRobotCount() > 5) {
                rc.construct(RobotType.PASTR);
                return;
            }

            // Sneak towards the enemy
            // TODO: waypoint shouldn't necessarily be enemy hq.
            // TODO: Check distress channels. Hack: Distress channels checked and broadcasted to
            // dependent upon your current map location.
            MapLocation waypoint = rc.senseEnemyHQLocation();
            BugNavigator.navigateTo(rc, waypoint);

            return;
        }
        catch (Exception e) {
            System.out.println(e);
            System.out.println("Soldier Exception");
        }
    }
}
