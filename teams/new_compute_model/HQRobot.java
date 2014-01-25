package new_compute_model;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;

public class HQRobot extends BaseRobot {

    public HQRobot(RobotController myRC) throws GameActionException {
        super(myRC);
    }

    protected void getUpdates() {
        // pass
    }

    protected void updateInternals() {
        // pass
    }

    protected void doAction() throws GameActionException {
        boolean doSpawn = false;
        Direction toSpawn = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
        // Check if a robot is spawnable and spawn one if it is
        if (rc.isActive() && rc.senseRobotCount() < GameConstants.MAX_ROBOTS) {
            for (int rot = 0; rot < 8; rot++) {
                if (rc.senseObjectAtLocation(rc.getLocation().add(toSpawn)) == null) {
                    doSpawn = true;
                    break;
                }
                toSpawn = toSpawn.rotateLeft();
            }
        }

        // Sense up to 15 because that is attack radius, but can splash to 21
        // Best attack is most kills, then most damage tiebreaker
        Team opponent = rc.getTeam().opponent();
        Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 15, opponent);

        MapLocation bestAttack = null;
        int bestKills = 0;
        double bestDamage = 0.0;

        for (int x = 0; x < nearbyEnemies.length; x++) {
            RobotInfo robotInfo = rc.senseRobotInfo(nearbyEnemies[x]);
            MapLocation robotLocation = robotInfo.location;

            // Do 50.0 damage to the target
            int kills = robotInfo.health <= 50.0 ? 1 : 0;
            double damage = Math.min(50.0, robotInfo.health);

            Robot[] splashAttack = rc.senseNearbyGameObjects(Robot.class, robotLocation, 2,
                    opponent);
            for (Robot s : splashAttack) {
                // Does 25.0 damage in the splash radius
                kills += rc.senseRobotInfo(s).health <= 25.0 ? 1 : 0;
                damage += Math.min(25.0, rc.senseRobotInfo(s).health);
            }
            if (bestKills < kills || (bestKills == kills && bestDamage < damage)) {
                bestKills = kills;
                bestDamage = damage;
                bestAttack = robotLocation;
            }
        }

        if (bestAttack == null) {
            Robot[] splashEnemies = rc.senseNearbyGameObjects(Robot.class, 21, opponent);
            if (splashEnemies.length != 0) {
                // Then we have an enemy outside our attack but not splash radius
                // just attack the first one that we can
                Robot targetEnemy = splashEnemies[0];
                RobotInfo info = rc.senseRobotInfo(targetEnemy);
                MapLocation robotLocation = info.location;
                Direction toMe = robotLocation.directionTo(rc.getLocation());
                bestAttack = robotLocation.add(toMe);
            }
        }

        if (bestAttack != null) {
            rc.attackSquare(bestAttack);
            return;
        }

        if (doSpawn) {
            rc.spawn(toSpawn);
        }
    }

    protected void sendUpdates() {
        // pass
    }

    protected void doCompute() {
        // pass
    }

}
