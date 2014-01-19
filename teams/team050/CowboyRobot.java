package team050;

import team050.rpc.Clans;
import team050.rpc.Clans.ClanMode;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class CowboyRobot extends BaseRobot {
    public final Team         me;
    public final Team         enemy;
    public int         clan;

    // This is the waypoint that our clan is moving towards
    public MapLocation target;

    // Prey is so that we can keep pursuing in the case of a chase
    public Robot       prey             = null;
    public MapLocation preyLocation     = null;
    // Predator is for if we are retreating or kamikazee
    public Robot       predator         = null;
    public MapLocation predatorLocation = null;

    private enum engagementBehavior {
        FIGHT, // We have an enemy in range and choose to attack
        RETREAT, // We are outnumbered and try to escape
        CHASE, // We see and are advantageous
        KAMIKAZEE, // We are hopeless and need to attack
        UNENGAGED, // This is the base type that we dont see enemies
    };

    // This is what we were thinking last turn
    public engagementBehavior type = engagementBehavior.UNENGAGED;

    public CowboyRobot(RobotController myRC) throws GameActionException {
        super(myRC);
        me = rc.getTeam();
        enemy = me.opponent();

        // Join first clan with less than 5 members.
        for (int i = 0; i < Clans.getNumClans() + 1; i++) {
            if (Clans.getSize(i) < 5) {
                Clans.joinClan(rc, i);
                clan = i;
                break;
            }
        }
        GeneralNavigation.setupNav(rc);
    }

    protected void getUpdates() throws GameActionException {
        MapLocation waypoint = Clans.getWaypoint(clan);
        if (!waypoint.equals(target)) {
            BugNavigator.bugReset();
            target = Clans.getWaypoint(clan);
        }
    }

    protected void updateInternals() throws GameActionException {
        // TODO: Try to avoid going near the enemy HQ

        switch (type) {
            case RETREAT:
                // Once we are out of sight, then we are unengaged
                Robot[] sightEnemies = rc.senseNearbyGameObjects(Robot.class, 35, enemy);
                if (sightEnemies.length == 0) {
                    BugNavigator.bugReset();
                    type = engagementBehavior.UNENGAGED;
                    break;
                }
                if (sightEnemies.length == 1) {
                    double enemyHealth = rc.senseRobotInfo(sightEnemies[0]).health;
                    if (rc.getHealth() > enemyHealth) {
                        BugNavigator.bugReset();
                        type = engagementBehavior.CHASE;
                        break;
                    }
                    // Check if it is the HQ
                    if (rc.senseRobotInfo(sightEnemies[0]).type == RobotType.HQ) {
                        type = engagementBehavior.RETREAT;
                        predator = sightEnemies[0];
                        predatorLocation = rc.senseRobotInfo(predator).location;
                        break;
                    }
                }
                if (!rc.canSenseObject(predator)) {
                    predator = sightEnemies[0];
                    // Update the predator
                    if (rc.senseRobotInfo(sightEnemies[0]).type == RobotType.HQ) {
                        if (sightEnemies.length > 1) {
                            predator = sightEnemies[1];
                        }
                        else {
                            type = engagementBehavior.UNENGAGED;
                        }
                    }
                    predatorLocation = rc.senseRobotInfo(predator).location;
                    break;
                }

                if (rc.getHealth() <= 40.0 && sightEnemies.length > 2) {
                    type = engagementBehavior.KAMIKAZEE;
                    break;
                }

                // This is if we cannot move away and have health problems
                // And the enemy has a tighter formation
                // TODO: Check if we have regrouped

                break;
            case KAMIKAZEE:
                // Just need a sanity check that there are still enemies
                sightEnemies = rc.senseNearbyGameObjects(Robot.class, 35, enemy);
                if (sightEnemies.length == 0) {
                    type = engagementBehavior.UNENGAGED;
                    break;
                }

                // Otherwise, keep rushing in. Do not change once we made the decision
                if (!rc.canSenseObject(predator)) {
                    Robot[] oneMoveEnemies = rc.senseNearbyGameObjects(Robot.class, 8, enemy);
                    if (oneMoveEnemies.length == 0) {
                        Robot[] twoMoveEnemies = rc.senseNearbyGameObjects(Robot.class, 15, enemy);
                        if (twoMoveEnemies.length == 0) {
                            predator = sightEnemies[0];
                        }
                        else {
                            predator = twoMoveEnemies[0];
                        }
                    }
                    else {
                        predator = oneMoveEnemies[0];
                    }
                }
                predatorLocation = rc.senseRobotInfo(predator).location;
                break;

            case UNENGAGED:
                // We need to determine whether we are in sight
                sightEnemies = rc.senseNearbyGameObjects(Robot.class, 35, enemy);
                if (sightEnemies.length > 0) {
                    // Always go check if we can get a better look
                    BugNavigator.bugReset();
                    type = engagementBehavior.CHASE;
                }
                break;

            case FIGHT:
            case CHASE:
                sightEnemies = rc.senseNearbyGameObjects(Robot.class, 35, enemy);
                if (sightEnemies.length == 0) {
                    BugNavigator.bugReset();
                    // Always go check if we can get a better look
                    type = engagementBehavior.UNENGAGED;
                    break;
                }

                int numRealEnemies = 0;
                for (Robot e : sightEnemies) {
                    RobotType enemyType = rc.senseRobotInfo(e).type;
                    numRealEnemies += enemyType == RobotType.SOLDIER ? 1 : 0;
                    numRealEnemies += enemyType == RobotType.HQ ? 10 : 0;
                }

                // This is smaller because we have to be close to consider ourselves to be up
                Robot[] sightFriendlies = rc.senseNearbyGameObjects(Robot.class, 15, me);
                boolean advantage = (sightFriendlies.length + 1) >= numRealEnemies;

                // We moved in last turn. Must make the Fight or Flight choice
                if (!advantage) {
                    type = engagementBehavior.RETREAT;
                    predator = sightEnemies[0];
                    predatorLocation = rc.senseRobotInfo(predator).location;
                    break;
                }
                Robot[] rangeEnemies = rc.senseNearbyGameObjects(Robot.class, 10, enemy);
                if (rangeEnemies.length > 0) {
                    type = engagementBehavior.FIGHT;
                    // always attack the weakest
                    double leastHealth = 999.9;
                    for (Robot enemyRobot : rangeEnemies) {
                        RobotInfo enemyInfo = rc.senseRobotInfo(enemyRobot);
                        // If I can kill it, then I should
                        if (enemyInfo.health <= 10.0) {
                            prey = enemyRobot;
                            preyLocation = rc.senseRobotInfo(prey).location;
                            break;
                        }
                        if (enemyInfo.health < leastHealth) {
                            leastHealth = enemyInfo.health;
                            prey = enemyRobot;
                        }
                    }
                }
                else {
                    type = engagementBehavior.CHASE;
                    if (prey == null || !rc.canSenseObject(prey)) {
                        prey = sightEnemies[0];
                        for (Robot e : sightEnemies) {
                            prey = rc.senseRobotInfo(e).health < rc.senseRobotInfo(prey).health ? e
                                    : prey;
                        }
                    }
                }
                preyLocation = rc.senseRobotInfo(prey).location;
                break;
        }
    }

    protected void doAction() throws GameActionException {
        switch (type) {
            case UNENGAGED:
                if (Clans.getClanMode(clan) == ClanMode.IDLE) {
                    BugNavigator.navigateTo(rc, rc.senseHQLocation());
                    // Do nothing when we are not active
                }
                else {
                    BugNavigator.navigateTo(rc, target);
                }
                break;

            case RETREAT:
                Direction toPredator = rc.getLocation().directionTo(predatorLocation);
                Direction away = toPredator.opposite();

                // Add 3 to be sufficiently far away
                MapLocation awayFromPredator = rc.getLocation().add(away, 3);

                // Reset for the best direction in the opposite direction
                BugNavigator.bugReset();
                BugNavigator.navigateTo(rc, awayFromPredator);
                break;

            case FIGHT:
                // If we can attack the prey, then do so and update whether it is alive
                if (rc.canAttackSquare(preyLocation)) {
                    // This should always be true
                    rc.attackSquare(preyLocation);
                    prey = rc.senseRobotInfo(prey).health <= 10.0 ? null : prey;
                }
                else {
                    type = engagementBehavior.UNENGAGED;
                }
                break;

            case CHASE:
                BugNavigator.navigateTo(rc, preyLocation);
                break;

            case KAMIKAZEE:
                Robot[] attackableEnemies = rc.senseNearbyGameObjects(Robot.class, 10, enemy);
                // This is the selfdestruct logic
                // The -1 is optimistic hope that one of them will die before they kill us
                if (rc.getHealth() < (attackableEnemies.length - 1) * 10.0) {
                    // We are doomed
                    Robot[] splashFriendlies = rc.senseNearbyGameObjects(Robot.class, 2, me);
                    Robot[] splashEnemies = rc.senseNearbyGameObjects(Robot.class, 2, enemy);
                    // If there are more enemies than friendlies
                    if (splashEnemies.length < splashFriendlies.length) {
                        rc.selfDestruct();
                        return;
                    }
                }

                toPredator = rc.getLocation().directionTo(predatorLocation);
                int bestEnemies = 0;
                Direction bestDirection = toPredator;
                if (rc.canMove(toPredator)) {
                    bestEnemies = rc.senseNearbyGameObjects(Robot.class,
                            rc.getLocation().add(toPredator), 2, enemy).length;
                }
                Direction left = toPredator.rotateLeft();
                if (rc.canMove(left)) {
                    int leftEnemies = rc.senseNearbyGameObjects(Robot.class,
                            rc.getLocation().add(left), 2, enemy).length;
                    if (bestEnemies < leftEnemies) {
                        bestDirection = left;
                        bestEnemies = leftEnemies;
                    }
                }
                Direction right = toPredator.rotateRight();
                if (rc.canMove(right)) {
                    int rightEnemies = rc.senseNearbyGameObjects(Robot.class,
                            rc.getLocation().add(right), 2, enemy).length;
                    if (bestEnemies < rightEnemies) {
                        bestDirection = right;
                        bestEnemies = rightEnemies;
                    }
                }
                rc.move(bestDirection);
                break;
        }

    }

    protected void sendUpdates() {
        // pass
    }

    protected void doCompute() {
        // pass
    }

}