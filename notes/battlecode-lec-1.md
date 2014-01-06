Lecture 1 (Draft)
=================

Read the [game specs][] for this year.

Objective
---------
- Herd cows into pastures. Seriously.
- Build pastures.
- Collect milk over time.

Install tutorial
----------------
- See official [install docs][].
- `ant run` to start matches

Map editor
----------
- Windows only
- Barely works at all (edit raw xml or make our own?)

Sample Player
-------------
- `rc.spawn(Direction.NORTH)`
  - Robot dies if you do not catch exceptions
- `rc.isActive()`
- switch on `rc.getType()`
- `rc.canMove(dir)`
- `rc.senseRobotCount()`

Action delay (AD)
-----------------
- Get 6000 bytecodes per round
- AD caused by:
  - moving
  - shooting
  - building (constructing)
  - doing computations
- GameConstants.java
  - `BYTECODE_PENALTY` constant (AD increase per bytecode used)

Sample player
-------------
TODO: Download this from website somewhere?
```
public class RobotPlayer {
  public static void run(RobotController rc) {
    while (true) {
      if (rc.getType() == RobotType.HQ) {
        Direction spawnDir = Direction.NORTH;
        try {
          if (rc.isActive() && rc.CanMove(spawnDir) && rc.senseRobotCount() ...
            rc.spawn(Direction.NORTH);
          }
        } catch (GameActionExecption e) {
          e.printStackTrace();
        }
      } else if (rc.getType()==RobotType.SOLDIER) {
        Direction allDirections[] = Direction.values();
        Direction chosenDirection = allDirections[(int)(Math.random()*8)];
        if(rc.isActive() && rc.canMove(chosenDirection)) {
          try {
            rc.move(chosenDirection);
          } catch (GameActionException e) {
            e.printStackTrace();
          }
        }
        Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class, 10000, rc.getTeam().opponent);
        if (enenyRobots.length > 0) {
          Robot anEnemy = enemyRobots[0];
          RobotInfo anEnemyInfo = rc.senseRobotInfo(anEnemy);
          if (anEnemyInfo.location.distanceSquaredTo(rc.getLocation()) < rc.getDistanceSquared()) { // ?
            if (rc.isActive()) {
              rc.attackSquare(anEnemyInfo.location);
            }
          }  // Need to surround with try/catch?
      }  
      rc.yield();
}
```

[game specs]: https://github.com/battlecode/battlecode-server/blob/2014-1.0.0/specs.md
[install docs]: http://s3.amazonaws.com/battlecode-releases-2014/docs/software.html
