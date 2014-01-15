package bug_bot.rpc;

import battlecode.common.MapLocation;

public class Marshaler {


    public static MapLocation intToMapLocation(int i) {
        int x = i / 100;
        int y = i % 100;
        return new MapLocation(x, y);
    }

    public static int MapLocationToInt(MapLocation ml) {
        return ml.x * 100 + ml.y;
    }
}
