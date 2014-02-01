package team050;

import team050.rpc.Channels;
import team050.rpc.Clans;
import team050.rpc.CoopNav;
import team050.rpc.Clans.ClanMode;
import team050.rpc.Liveness;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.TerrainTile;

public class CowboyRobot extends BaseRobot {
    public final Team  me;
    public final Team  enemy;
    public static int  clan             = -1;
    public MapLocation myLoc;

    public int         gid              = -1;

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

        // Join first clan that needs new members.
        for (int pgid = 0; pgid < Channels.MAX_ROBOTS; pgid++) {
            int pclan = pgid / 10;
            if (Clans.getClanSize(pclan) >= Clans.TARGET_CLAN_SIZES[pclan]) {
                continue;
            }
            if (Liveness.getLastPostedRoundByGid(pgid) == 0) {
                gid = pgid;
                clan = gid / 10;  // TODO: 10 should be some constant somewhere or something.
                Clans.setClanSize(clan, Clans.getClanSize(clan) + 1);
                Liveness.updateLiveness(RobotType.SOLDIER, gid);
                break;
            }
        }
        assert (gid != -1);
        assert (clan != -1);
    }

    protected void getUpdates() throws GameActionException {
        MapLocation waypoint = Clans.getWaypoint(clan);
        if (!waypoint.equals(target)) {
            BugNavigator.bugReset();
            target = waypoint;
        }

        // Do not just build a structure so that it can get destroyed
        if (rc.isConstructing() && type != engagementBehavior.UNENGAGED
                && rc.getConstructingRounds() < 3 && rc.getConstructingType() == RobotType.PASTR) {
            Robot[] friends = rc.senseNearbyGameObjects(Robot.class, 35, me);
            Robot[] enemies = rc.senseNearbyGameObjects(Robot.class, 35, enemy);
            if (friends.length < enemies.length) {
                rc.selfDestruct();
            }
        }
    }

    protected void updateInternals() throws GameActionException {
        myLoc = rc.getLocation();

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

                if (rc.getHealth() <= 60.0 && sightEnemies.length > 2) {
                    // Hack to save bytecode for important turns
                    if (!rc.isActive()) {
                        Direction toPredator = myLoc.directionTo(predatorLocation);
                        boolean myEnd = false;
                        if (checkTerrain(myLoc.add(toPredator))) {
                            myEnd = true;
                        }
                        else {
                            myEnd = checkTerrain(myLoc.add(toPredator.rotateLeft()))
                                    || checkTerrain(myLoc.add(toPredator.rotateRight()));
                        }

                        if (!myEnd) {
                            break;
                        }

                        boolean theirEnd = false;
                        Direction fromPredator = toPredator.opposite();
                        if (checkTerrain(predatorLocation.add(fromPredator))) {
                            theirEnd = true;
                        }
                        else {
                            theirEnd = checkTerrain(myLoc.add(fromPredator.rotateLeft()))
                                    || checkTerrain(myLoc.add(fromPredator.rotateRight()));
                        }

                        if (!theirEnd) {
                            break;
                        }
                        // This is a rough test since it does not check the middle, but it is a
                        // quick heuristic
                        type = engagementBehavior.KAMIKAZEE;
                    }
                    break;
                }

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
                    prey = sightEnemies[0];
                    preyLocation = rc.senseRobotInfo(prey).location;
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
                    switch (enemyType) {
                        case SOLDIER:
                            numRealEnemies += 1;
                            break;
                        case HQ:
                            numRealEnemies += 100;
                            break;
                        case NOISETOWER:
                        case PASTR:
                            break;
                    }
                }

                // This is smaller because we have to be close to consider ourselves to be up
                Robot[] sightFriendlies = rc.senseNearbyGameObjects(Robot.class, 15, me);
                int actualSightFriendlies = 0;
                for (Robot r : sightFriendlies) {
                    actualSightFriendlies += rc.senseRobotInfo(r).type == RobotType.SOLDIER ? 1 : 0;
                }
                boolean advantage = ((actualSightFriendlies + 1) >= numRealEnemies && (rc
                        .getHealth() > 10.0 || (numRealEnemies == 1 && rc
                        .senseRobotInfo(sightEnemies[0]).health <= 10.0)));

                // We moved in last turn. Must make the Fight or Flight choice
                if (!advantage) {
                    type = engagementBehavior.RETREAT;
                    predator = sightEnemies[0];
                    predatorLocation = rc.senseRobotInfo(predator).location;
                    break;
                }
                Robot[] rangeEnemies = rc.senseNearbyGameObjects(Robot.class, 10, enemy);
                if (rangeEnemies.length > 0) {
                    prey = rangeEnemies[0];
                    preyLocation = rc.senseRobotInfo(prey).location;
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
                        if (enemyInfo.health < leastHealth
                                && enemyInfo.type != RobotType.NOISETOWER) {
                            leastHealth = enemyInfo.health;
                            prey = enemyRobot;
                            preyLocation = rc.senseRobotInfo(prey).location;
                        }
                    }
                }
                else {
                    type = engagementBehavior.CHASE;
                    if (prey == null || !rc.canSenseObject(prey)) {
                        prey = sightEnemies[0];
                        for (Robot e : sightEnemies) {
                            prey = myLoc.distanceSquaredTo(rc.senseRobotInfo(e).location) < myLoc
                                    .distanceSquaredTo(rc.senseRobotInfo(prey).location) ? e : prey;
                        }
                    }
                }
                preyLocation = rc.senseRobotInfo(prey).location;
                break;
        }
    }

    protected void doAction() throws GameActionException {
        double rangeSquared = 2;
        switch (type) {
            case UNENGAGED:
                switch (Clans.getClanMode(clan)) {
                    case IDLE:
                        rc.sneak(BugNavigator.getDirectionTo(rc, rc.senseHQLocation()));
                        // Do nothing when we are not active
                        break;
                    case BUILDER:
                        // If "close enough to target" build a PASTR if clan hasn't built one yet.
                        // If PASTR built and close enough, build NT.
                        if (withinRangeSquared(target, rangeSquared)) {
                            if (Clans.getClanPastrStatus(clan) == false) {
                                Clans.setClanPastrStatus(clan, true);
                                Robot[] myPastrs = rc.senseNearbyGameObjects(Robot.class, 2, me);
                                boolean pastr = false;
                                for (Robot a : myPastrs) {
                                    if (rc.senseRobotInfo(a).type == RobotType.PASTR) {
                                        pastr = true;
                                    }
                                }
                                if (!pastr) {
                                    Clans.setWaypoint(clan, myLoc);
                                    rc.construct(RobotType.PASTR);
                                    CoopNav.requestComputation(myLoc, GeneralNavigation.coarseness);
                                }
                            }
                            else if (Clans.getClanNTStatus(clan) == false) {
                                Clans.setClanNTStatus(clan, true);
                                rc.construct(RobotType.NOISETOWER);
                                Clans.setClanMode(clan, ClanMode.DEFENDER);
                            }
                        }
                        else {
                            GeneralNavigation.setTarget(target);
                            rc.move(GeneralNavigation.getNextDirection(rc));
                        }
                        break;
                    case DEFENDER:
                        /* Does our clan need us to rebuild anything? */
                        if (Clans.getClanPastrStatus(clan) == false) {
                            if (withinRangeSquared(target, rangeSquared)) {
                                Clans.setClanPastrStatus(clan, true);
                                Robot[] myPastrs = rc.senseNearbyGameObjects(Robot.class, 2, me);
                                boolean pastr = false;
                                for (Robot a : myPastrs) {
                                    if (rc.senseRobotInfo(a).type == RobotType.PASTR) {
                                        pastr = true;
                                    }
                                }
                                if (!pastr) {
                                    Clans.setWaypoint(clan, myLoc);
                                    rc.construct(RobotType.PASTR);
                                    CoopNav.requestComputation(myLoc, GeneralNavigation.coarseness);
                                }
                            }
                            else {
                                rc.move(BugNavigator.getDirectionTo(rc, target));
                            }
                        }
                        else if (Clans.getClanNTStatus(clan) == false) {
                            if (withinRangeSquared(target, rangeSquared)) {
                                Clans.setClanNTStatus(clan, true);
                                rc.construct(RobotType.NOISETOWER);
                                Clans.setClanMode(clan, ClanMode.DEFENDER);
                            }
                            else {
                                GeneralNavigation.setTarget(target);
                                rc.move(GeneralNavigation.getNextDirection(rc));
                            }
                        }
                        else {
                            Defense.initDirs(rc);
                            Defense.doDefense(rc);
                        }
                        break;
                    case RAIDER:
                        GeneralNavigation.setTarget(target);
                        rc.move(GeneralNavigation.getNextDirection(rc));
                        break;
                    default:
                        rc.move(BugNavigator.getDirectionTo(rc, target));
                        break;
                }
                break;

            case RETREAT:
                Direction toPredator = myLoc.directionTo(predatorLocation);
                Direction away = toPredator.opposite();

                // Add 3 to be sufficiently far away
                MapLocation awayFromPredator = myLoc.add(away, 3);

                // Reset for the best direction in the opposite direction
                BugNavigator.bugReset();
                Direction dirToMove = BugNavigator.getDirectionTo(rc, awayFromPredator);

                // If we are trying to move in a direction that is not next to away, then we stuck
                int diff = Math.abs(dirToMove.ordinal() - away.ordinal());
                if (diff > 1 && diff < 7) {
                    type = engagementBehavior.KAMIKAZEE;
                    // TODO: Make a heuristic to see if we will reach the enemy in time to avoid
                    // running into wall and then getting shot down
                    doAction();
                    break;
                }

                if (Clans.getClanMode(clan) == ClanMode.DEFENDER) {
                    MapLocation pastr = Defense.pastr;
                    if (pastr != null && rc.canSenseSquare(pastr)) {
                        Robot myPastr = (Robot) rc.senseObjectAtLocation(pastr);
                        if (myPastr != null) {
                            RobotInfo r = rc.senseRobotInfo(myPastr);
                            if (r.health <= 10.0 && rc.canAttackSquare(pastr)) {
                                rc.attackSquare(pastr);
                                break;
                            }
                        }
                    }
                }

                rc.move(dirToMove);

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
                    doAction();
                }
                break;

            case CHASE:
                if (Clans.getClanMode(clan) == ClanMode.DEFENDER) {
                    MapLocation pastr = Defense.pastr;
                    if (pastr != null && rc.canSenseSquare(pastr)) {
                        Robot myPastr = (Robot) rc.senseObjectAtLocation(pastr);
                        if (myPastr != null) {
                            RobotInfo r = rc.senseRobotInfo(myPastr);
                            if (r.health <= 10.0 && rc.canAttackSquare(pastr)) {
                                rc.attackSquare(pastr);
                                break;
                            }
                        }
                    }
                }

                Robot[] sightFriendlies = rc.senseNearbyGameObjects(Robot.class, 15, me);
                Robot[] sightEnemies = rc.senseNearbyGameObjects(Robot.class, 35, enemy);
                if (sightFriendlies.length == 0 && sightEnemies.length == 1) {
                    // We are in a 1-1 situation and should not be the first into striking range
                    prey = sightEnemies[0];
                    RobotInfo preyInfo = rc.senseRobotInfo(prey);
                    preyLocation = preyInfo.location;
                    int enemyDist = myLoc.distanceSquaredTo(preyLocation);
                    if (enemyDist > 15 && enemyDist <= 22) {
                        if (preyInfo.health + 10.0 >= rc.getHealth()) {
                            // This is where we stand still and should not close the gap ourselves
                            break;
                        }
                    }
                }
                rc.move(BugNavigator.getDirectionTo(rc, preyLocation));
                break;

            case KAMIKAZEE:
                toPredator = myLoc.directionTo(predatorLocation);
                int bestEnemies = 0;
                Direction bestDirection = toPredator;
                if (rc.canMove(toPredator)) {
                    bestEnemies = rc.senseNearbyGameObjects(Robot.class, myLoc.add(toPredator), 2,
                            enemy).length;
                }
                Direction left = toPredator.rotateLeft();
                if (rc.canMove(left)) {
                    int leftEnemies = rc.senseNearbyGameObjects(Robot.class, myLoc.add(left), 2,
                            enemy).length;
                    if (bestEnemies < leftEnemies) {
                        bestDirection = left;
                        bestEnemies = leftEnemies;
                    }
                }
                Direction right = toPredator.rotateRight();
                if (rc.canMove(right)) {
                    int rightEnemies = rc.senseNearbyGameObjects(Robot.class, myLoc.add(right), 2,
                            enemy).length;
                    if (bestEnemies < rightEnemies) {
                        bestDirection = right;
                        bestEnemies = rightEnemies;
                    }
                }

                if (rc.canMove(bestDirection)) {
                    rc.move(bestDirection);
                }
                else {
                    type = engagementBehavior.RETREAT;
                    if (Clock.getBytecodeNum() < 2500) {
                        doAction();
                    }
                }
                break;
        }

    }

    private boolean withinRangeSquared(MapLocation target, double rangeSquared) {
        return target.distanceSquaredTo(myLoc) < rangeSquared;
    }

    protected void sendUpdates() throws GameActionException {
        if (!rc.isActive()) {
            if (!rc.isConstructing()) {
                Liveness.updateLiveness(RobotType.SOLDIER, gid);
            }
            else {
                Liveness.updateLiveness(rc.getConstructingType(), gid);
            }
        }
    }

    protected void doCompute() throws GameActionException {
        if (type == engagementBehavior.KAMIKAZEE) {
            Robot[] attackableEnemies = rc.senseNearbyGameObjects(Robot.class, 10, enemy);
            int realEnemies = 0;
            for (Robot r : attackableEnemies) {
                switch (rc.senseRobotInfo(r).type) {
                    case SOLDIER:
                        realEnemies += 1;
                        break;
                    case HQ:
                        realEnemies -= 100;
                        break;
                    case NOISETOWER:
                    case PASTR:
                        break;
                }
            }
            // This is the selfdestruct logic
            Robot[] splashFriendlies = rc.senseNearbyGameObjects(Robot.class, 2, me);
            if (rc.getHealth() < realEnemies * 10.0 + 20.0) {
                // We are doomed
                Robot[] splashEnemies = rc.senseNearbyGameObjects(Robot.class, 2, enemy);
                // If there are more enemies than friendlies
                if (splashEnemies.length > splashFriendlies.length) {
                    rc.selfDestruct();
                    return;
                }
            }

            if (splashFriendlies.length == 0) {
                Robot[] splashEnemies = rc.senseNearbyGameObjects(Robot.class, 2, enemy);
                for (Robot r : splashEnemies) {
                    RobotInfo info = rc.senseRobotInfo(r);
                    switch (info.type) {
                        case SOLDIER:
                            rc.selfDestruct();
                            return;
                        case HQ:
                            break;
                        case NOISETOWER:
                        case PASTR:
                            if (info.health <= 41.0 + rc.getHealth() * .5) {
                                rc.selfDestruct();
                                return;
                            }
                    }
                }
            }

        }
    }

    private boolean checkTerrain(MapLocation loc) {
        TerrainTile t = rc.senseTerrainTile(loc);
        switch (t) {
            case NORMAL:
                return true;
            case ROAD:
                return true;
            case VOID:
                return false;
            case OFF_MAP:
                return false;
        }
        return false;
    }

}
