package team050.rpc;

import battlecode.common.MapLocation;

public class CoopNav {
    /*
     * ================================= =================================
     * 
     * Methods that a COWBOY would call.
     * 
     * ================================= =================================
     */

    /*
     * Return false if nobody has even requested that as a computation yet.
     */
    public static boolean isComputationReady(MapLocation target) {
        return false;
    }

    /*
     * Return false if no room for more computations.
     */
    public static boolean requestComputation(MapLocation target, int coarseness) {
        return false;
    }

    /*
     * Return -1 if computation not ready or not requested.
     */
    public static int getDirectionFromResult(MapLocation target, int x, int y) {
        return -1;
    }

    /*
     * ================================= =================================
     * 
     * Methods that a PASTR would call.
     * 
     * ================================= =================================
     */

    public static boolean isWorkAvailable() {
        return false;
    }

    /*
     * int returned is a "jobid". You need to use this to call some other methods.
     * 
     * Return -1 if no job available.
     */
    public static int claimNextAvailableJob() {
        return -1;
    }

    /*
     * return -1 if jobId is invalid (doesn't correspond to an active job).
     */
    public static MapLocation getJobTarget(int jobId) {
        return null;
    }

    /*
     * return -1 if jobId is invalid (doesn't correspond to an active job).
     */
    public static int getJobCoarseness(int jobId) {
        return -1;
    }

    /*
     * Post a finished computation to shared memory.
     */
    public static void postJobResult(int jobId, int[][] result) {
        return;
    }
}
