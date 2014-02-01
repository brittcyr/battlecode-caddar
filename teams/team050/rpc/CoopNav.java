package team050.rpc;

import battlecode.common.GameActionException;
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
     * 
     * Otherwise, return true.
     */
    public static boolean isComputationReady(MapLocation target) throws GameActionException {
        for (int i = 0; i < Channels.MAX_NAV_REQUESTS; i++) {
            int[] navreqDescriptor = getNavreqDescriptor(i);
            MapLocation nrdTarget = getTargetFromNrd(navreqDescriptor);

            if ((nrdTarget.x == target.x) && (nrdTarget.y == target.y)) {
                return isFinishedFromNrd(navreqDescriptor);
            }
        }

        return false;
    }

    /*
     * Return -1 if no room for more computations.
     * 
     * Return -2 if computation has already been requested.
     * 
     * Return 0 if request is posted.
     */
    public static int requestComputation(MapLocation target, int coarseness) throws Exception {
        for (int i = 0; i < Channels.MAX_NAV_REQUESTS; i++) {
            int[] navreqDescriptor = getNavreqDescriptor(i);

            // Check if computation has already been requested.
            MapLocation nrdTarget = getTargetFromNrd(navreqDescriptor);
            int nrdCoarseness = getCoarsenessFromNrd(navreqDescriptor);
            if ((nrdTarget.x == target.y) && (nrdTarget.y == target.y)
                    && (nrdCoarseness == coarseness)) {
                return -2;
            }

            // Post request if job i not already taken.
            boolean nrdRequested = isRequestedFromNrd(navreqDescriptor);
            if (!nrdRequested) {
                setCoarsenessByJob(coarseness, i);
                setTargetByJob(target, i);
                setRequestedByJob(true, i);
                setComputingByJob(false, i);
                setFinishedByJob(false, i);
                setRoundByJob(0, i);
                return 0;
            }
        }

        // Checked all possible jobs, and there wasn't any room.
        return -1;
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

    /*
     * ================================= =================================
     * 
     * Private helper methods.
     * 
     * ================================= =================================
     */

    private static MapLocation getTargetFromNrd(int[] navreqDescriptor) {
        int word = navreqDescriptor[0] / 2;
        return Marshaler.intToMapLocation(word);
    }

    private static void setTargetByJob(MapLocation target, int jobId) throws GameActionException {
        int word = Radio.getData(Channels.NAVREQ_HEADER_TBL
                + (jobId * Channels.NAVREQ_HEADER_DESC_SZ), 1)[0];
        int coarseness = word & 1;

        int newWord = Marshaler.MapLocationToInt(target);
        newWord = newWord << 1;
        newWord |= coarseness;

        Radio.putData(Channels.NAVREQ_HEADER_TBL + (jobId * Channels.NAVREQ_HEADER_DESC_SZ),
                new int[] { newWord });
    }

    private static boolean isFinishedFromNrd(int[] navreqDescriptor) {
        int word = navreqDescriptor[1];
        return ((word & (1 << 2)) != 0) ? true : false;
    }

    private static boolean isRequestedFromNrd(int[] navreqDescriptor) {
        int word = navreqDescriptor[1];
        return ((word & 1) != 0) ? true : false;
    }

    /*
     * Coarseness is always 2 or 3, so we stash it in the lowest bit of the first word in the
     * descriptor.
     * 
     * Convention: 0 => c=2, 1 => c=3.
     */
    private static int getCoarsenessFromNrd(int[] navreqDescriptor) {
        int word = navreqDescriptor[0];
        if (word % 2 == 0) {
            return 2;
        }
        else {
            return 3;
        }
    }

    private static void setCoarsenessByJob(int coarseness, int jobId) throws Exception {
        if ((coarseness != 2) && (coarseness != 3)) {
            throw new Exception("Coarsness values of only 2 or 3 are supported");
        }
        int word = Radio.getData(Channels.NAVREQ_HEADER_TBL
                + (jobId * Channels.NAVREQ_HEADER_DESC_SZ) + 1, 1)[0];
        if (coarseness == 2) {
            word &= ~1;
        }
        else {
            word |= 1;
        }
        Radio.putData(Channels.NAVREQ_HEADER_TBL + (jobId * Channels.NAVREQ_HEADER_DESC_SZ) + 1,
                new int[] { word });
    }

    private static int[] getNavreqDescriptor(int jobId) throws GameActionException {
        return Radio.getData(Channels.NAVREQ_HEADER_TBL + jobId * Channels.NAVREQ_HEADER_DESC_SZ,
                Channels.NAVREQ_HEADER_DESC_SZ);
    }

    private static void setRoundByJob(int i, int i2) {
        // TODO Auto-generated method stub

    }

    private static void setFinishedByJob(boolean b, int i) {
        // TODO Auto-generated method stub

    }

    private static void setComputingByJob(boolean b, int i) {
        // TODO Auto-generated method stub

    }

    private static void setRequestedByJob(boolean b, int i) {
        // TODO Auto-generated method stub

    }

}
