package pastr_attacker;

import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class CowboyRobot extends BaseRobot {
    static Random       rand;
    private boolean     pastrAttacker;
    private Direction[] directions = { Direction.NORTH, Direction.NORTH_EAST, Direction.EAST,
            Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST,
            Direction.NORTH_WEST  };

    public CowboyRobot(RobotController myRC) throws GameActionException {
        super(myRC);
        rand = new Random();
        pastrAttacker = false;
    }

    public void run() {
        try {
            if (rc.readBroadcast(1001) == 1 && !pastrAttacker) {
                rc.broadcast(1001, 0);
                pastrAttacker = true;
            }
            if (pastrAttacker) {
                rc.broadcast(1000, rc.readBroadcast(1000) + 1);
            }

            // Attack if possible prioritize attacking the enemy robots first
            Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 10, rc.getTeam()
                    .opponent());
            for (int x = 0; x < nearbyEnemies.length; x++) {
                RobotInfo robotInfo = rc.senseRobotInfo(nearbyEnemies[x]);
                if (robotInfo.type == RobotType.SOLDIER) {
                    rc.attackSquare(robotInfo.location);
                    return;
                }
            }
            for (int x = 0; x < nearbyEnemies.length; x++) {
                RobotInfo robotInfo = rc.senseRobotInfo(nearbyEnemies[x]);
                if (robotInfo.type != RobotType.HQ) {
                    rc.attackSquare(robotInfo.location);
                    return;
                }
            }

            if (pastrAttacker && rc.sensePastrLocations(rc.getTeam().opponent()).length > 0) {
                MapLocation enemyPastrs[] = rc.sensePastrLocations(rc.getTeam().opponent());
                int closestInd = 0;
                int bestDist = 9999;
                for (int i = 0; i < enemyPastrs.length; i++) {
                    if (rc.getLocation().distanceSquaredTo(enemyPastrs[i]) < bestDist) {
                        bestDist = rc.getLocation().distanceSquaredTo(enemyPastrs[i]);
                        closestInd = i;
                    }
                } // Run to attack the pastr
                MapLocation pastr = enemyPastrs[closestInd];
                Direction toPastr = rc.getLocation().directionTo(pastr);
                if (rc.canMove(toPastr)) {
                    rc.move(toPastr);
                    return;
                }
                else {

                    for (int j = 0; j < 8; j++) {
                        if (this.directions[(j + 1) % 8].equals(toPastr)
                                && rc.canMove(this.directions[j])) {
                            rc.move(this.directions[j]);
                            return;
                        }
                    }

                    for (int j = 0; j < 8; j++) {
                        // Move perpendicularly and getting closer
                        if (directions[(j + 2) % 8].equals(toPastr)
                                && rc.canMove(directions[j])
                                && rc.getLocation().add(directions[j]).distanceSquaredTo(pastr) < rc
                                        .getLocation().add(directions[(j + 4) % 8])
                                        .distanceSquaredTo(pastr)) {
                            rc.move(directions[j]);
                            return;
                        }
                    }
                    this.pastrAttacker = false;
                }
            }

            int action = (rc.getRobot().getID() * rand.nextInt(101) + 50) % 101;
            if (rc.senseCowsAtLocation(rc.getLocation()) > 2000
                    && rc.senseNearbyGameObjects(Robot.class, 30, rc.getTeam().opponent()).length == 0) {
                rc.construct(RobotType.PASTR);
                return;
            }

            // Construct a PASTR
            if (action < 1 && rc.getLocation().distanceSquaredTo(rc.senseHQLocation()) > 20
                    && rc.senseCowGrowth()[rc.getLocation().x][rc.getLocation().y] > 0
                    && rc.senseRobotCount() > 5) {
                rc.construct(RobotType.PASTR);
            }
            else if (action < 40) {
                // Random movement
                Direction moveDirection = directions[rand.nextInt(8)];
                if (rc.canMove(moveDirection)) {
                    rc.sneak(moveDirection);
                }
            }
            else {
                // Sneak towards the enemy
                Direction toEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
                if (rc.canMove(toEnemy)) {
                    rc.sneak(toEnemy);
                }
            }

        }
        catch (Exception e) {
            System.out.println(e);
            System.out.println("Soldier Exception");
        }
    }
}
