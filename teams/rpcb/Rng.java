package rpcb;

import java.util.Random;

import battlecode.common.Clock;

public class Rng {
    private static Random rnd = new Random(Clock.getRoundNum());

    public static int nextInt(int n) {
        return rnd.nextInt(n);
    }
}
