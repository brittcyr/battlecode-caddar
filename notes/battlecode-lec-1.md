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

Sample Player Code
-------------
```java
package bob;

//this is the code from the first Battlecode 2014 lecture
//paste this text into RobotPlayer.java in a package called bob
//this code is badly organized. We'll fix it in later lectures.
//you can use this as a reference for how to use certain methods.

import battlecode.common.*;

public class RobotPlayer{
  
  public static void run(RobotController rc){
    while(true){
      if(rc.getType()==RobotType.HQ){//if I'm a headquarters
        Direction spawnDir = Direction.NORTH;
        try {
          if(rc.isActive()&&rc.canMove(spawnDir)&&rc.senseRobotCount()<GameConstants.MAX_ROBOTS){
            rc.spawn(Direction.NORTH);
          }
        } catch (GameActionException e) {
          // TODO hi contestant who downloaded this.
          e.printStackTrace();
        }
      }else if(rc.getType()==RobotType.SOLDIER){
        //shooting
        Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent());
        if(enemyRobots.length>0){//if there are enemies
          Robot anEnemy = enemyRobots[0];
          RobotInfo anEnemyInfo;
          try {
            anEnemyInfo = rc.senseRobotInfo(anEnemy);
            if(anEnemyInfo.location.distanceSquaredTo(rc.getLocation())<rc.getType().attackRadiusMaxSquared){
              if(rc.isActive()){
                rc.attackSquare(anEnemyInfo.location);
              }
            }
          } catch (GameActionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }else{//there are no enemies, so build a tower
          if(Math.random()<0.01){
            if(rc.isActive()){
              try {
                rc.construct(RobotType.PASTR);
              } catch (GameActionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
            }
          }
        }
        //movement
        Direction allDirections[] = Direction.values();
        Direction chosenDirection = allDirections[(int)(Math.random()*8)];
        if(rc.isActive()&&rc.canMove(chosenDirection)){
          try {
            rc.move(chosenDirection);
          } catch (GameActionException e) {
            e.printStackTrace();
          }
        }
      }
      rc.yield();
    }
  }
}
```

[game specs]: https://github.com/battlecode/battlecode-server/blob/2014-1.0.0/specs.md
[install docs]: http://s3.amazonaws.com/battlecode-releases-2014/docs/software.html
