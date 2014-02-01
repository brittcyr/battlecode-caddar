package team050.rpc;

import team050.GeneralNavigation;
import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

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
            int coarseX = target.x / GeneralNavigation.coarseness;
            int coarseY = target.y / GeneralNavigation.coarseness;
            MapLocation nrdTarget = getTargetFromNrd(navreqDescriptor);
            int coarseNrdX = nrdTarget.x / GeneralNavigation.coarseness;
            int coarseNrdY = nrdTarget.y / GeneralNavigation.coarseness;

            if ((coarseX == coarseNrdX) && (coarseY == coarseNrdY)) {
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
    public static int requestComputation(MapLocation target, int coarseness)
            throws GameActionException {
        for (int i = 0; i < Channels.MAX_NAV_REQUESTS; i++) {
            int[] navreqDescriptor = getNavreqDescriptor(i);

            // Check if computation has already been requested.
            int coarseX = target.x / GeneralNavigation.coarseness;
            int coarseY = target.y / GeneralNavigation.coarseness;
            MapLocation nrdTarget = getTargetFromNrd(navreqDescriptor);
            int coarseNrdX = nrdTarget.x / GeneralNavigation.coarseness;
            int coarseNrdY = nrdTarget.y / GeneralNavigation.coarseness;
            if ((coarseX == coarseNrdX) && (coarseY == coarseNrdY)
                    && (GeneralNavigation.coarseness == coarseness)) {
                return -2;
            }

            // Post request if job i not already taken.
            boolean nrdRequested = isRequestedFromNrd(navreqDescriptor);
            if (!nrdRequested) {
                navreqDescriptor[0] = setCoarsenessInWord(coarseness, navreqDescriptor[0]);
                navreqDescriptor[0] = setTargetInWord(target, navreqDescriptor[0]);
                navreqDescriptor[1] = setRequestedInWord(true, navreqDescriptor[1]);
                navreqDescriptor[1] = setComputingInWord(false, navreqDescriptor[1]);
                navreqDescriptor[1] = setFinishedInWord(false, navreqDescriptor[1]);
                navreqDescriptor[1] = setRoundInWord(0, navreqDescriptor[1]);
                setNavreqDescriptor(navreqDescriptor, i);
                return 0;
            }
        }

        // Checked all possible jobs, and there wasn't any room.
        return -1;
    }

    /*
     * Return -1 if computation not ready or not requested.
     */
    public static int getDirectionFromResult(RobotController rc, MapLocation target, int x, int y)
            throws GameActionException {
        for (int i = 0; i < Channels.MAX_NAV_REQUESTS; i++) {
            int[] navreqDescriptor = getNavreqDescriptor(i);
            MapLocation nrdTarget = getTargetFromNrd(navreqDescriptor);
            int coarseX = target.x / GeneralNavigation.coarseness;
            int coarseY = target.y / GeneralNavigation.coarseness;
            int coarseNrdX = nrdTarget.x / GeneralNavigation.coarseness;
            int coarseNrdY = nrdTarget.y / GeneralNavigation.coarseness;

            if ((coarseX == coarseNrdX) && (coarseY == coarseNrdY)) {
                int index = (int) Math.ceil(rc.getMapWidth() / GeneralNavigation.coarseness) * y
                        + x;
                int navresultSize = ((int) Math.ceil(rc.getMapWidth()
                        / GeneralNavigation.coarseness))
                        * ((int) Math.ceil(rc.getMapHeight() / GeneralNavigation.coarseness));
                int navresultAddr = Channels.NAVRESULTS + i * navresultSize;
                int dxnAddr = navresultAddr + index;
                int dxn = Radio.getData(dxnAddr, 1)[0];
                return dxn;
            }
        }

        return -1;
    }

    /*
     * ================================= =================================
     * 
     * Methods that a PASTR would call.
     * 
     * ================================= =================================
     */

    /*
     * int returned is a "jobid". You need to use this to call some other methods.
     * 
     * Return -1 if no job available.
     */
    public static MapLocation claimNextAvailableJob() throws GameActionException {
        for (int i = 0; i < Channels.MAX_NAV_REQUESTS; i++) {
            int[] navreqDescriptor = getNavreqDescriptor(i);

            if (isRequestedFromNrd(navreqDescriptor)) {
                if (!(isComputingFromNrd(navreqDescriptor))) {
                    if (!(isFinishedFromNrd(navreqDescriptor))) {
                        // Job requested, nobody computing solution, and no answer. Claim it!
                        navreqDescriptor[1] = setComputingInWord(true, navreqDescriptor[1]);
                        navreqDescriptor[1] = setFinishedInWord(false, navreqDescriptor[1]);
                        navreqDescriptor[1] = setRoundInWord(Clock.getRoundNum(),
                                navreqDescriptor[1]);
                        setNavreqDescriptor(navreqDescriptor, i);
                        return getTargetFromNrd(navreqDescriptor);
                    }
                }
            }
        }

        return null;
    }

    /*
     * Post a finished computation to shared memory.
     * 
     * This performs NO SAFETY CHECKS!!! It will just write to the radio blindly.
     * 
     * If you start computing on a job and take too long and the HQ clears you from the navreq
     * header table, then when you post this, you might be overwriting somebody else's request,
     * giving everyone the wrong data when they think it's right!
     * 
     * Return -1 if the job to compute paths to target isn't in the header table anymore.
     */
    public static int postJobResult(RobotController rc, MapLocation target, int[][] result)
            throws GameActionException {

        int jobId = -1;
        for (int i = 0; i < Channels.MAX_NAV_REQUESTS; i++) {
            int[] navreqDescriptor = getNavreqDescriptor(i);
            MapLocation nrdTarget = getTargetFromNrd(navreqDescriptor);
            int coarseX = target.x / GeneralNavigation.coarseness;
            int coarseY = target.y / GeneralNavigation.coarseness;
            int coarseNrdX = nrdTarget.x / GeneralNavigation.coarseness;
            int coarseNrdY = nrdTarget.y / GeneralNavigation.coarseness;
            if ((coarseX == coarseNrdX) && (coarseY == coarseNrdY)) {
                jobId = i;
                break;
            }
        }
        if (jobId == -1) {
            return -1;
        }

        int coarseness = GeneralNavigation.coarseness;
        int navresultSize = ((int) Math.ceil(rc.getMapWidth() / coarseness))
                * ((int) Math.ceil(rc.getMapHeight() / coarseness));
        int navresultAddr = Channels.NAVRESULTS + jobId * navresultSize;
        int ySize = (int) Math.ceil(rc.getMapHeight() / coarseness);

        int[] navreqDescriptor = getNavreqDescriptor(jobId);
        navreqDescriptor[1] = setFinishedInWord(true, navreqDescriptor[1]);
        navreqDescriptor[1] = setComputingInWord(false, navreqDescriptor[1]);
        setNavreqDescriptor(navreqDescriptor, jobId);

        // Post each row at a time.
        for (int y = 0; y < result[0].length; y++) {
            int rowAddr = navresultAddr + y * ySize;
            Radio.putData(rowAddr, result[y]);
        }

        return 0;
    }

    /*
     * ================================= =================================
     * 
     * Private helper methods.
     * 
     * ================================= =================================
     * 
     * TODO: Factor out setting and reading bits into own method.
     * 
     * TODO: Use constants for bit positions.
     */

    private static MapLocation getTargetFromNrd(int[] navreqDescriptor) {
        int word = navreqDescriptor[0] / 2;
        return Marshaler.intToMapLocation(word);
    }

    private static int setTargetInWord(MapLocation target, int word) {
        int coarseness = word & 1;
        word = Marshaler.MapLocationToInt(target);
        word = word << 1;
        word |= coarseness;
        return word;
    }

    private static boolean isFinishedFromNrd(int[] navreqDescriptor) {
        int word = navreqDescriptor[1];
        return ((word & (1 << 2)) != 0) ? true : false;
    }

    private static boolean isRequestedFromNrd(int[] navreqDescriptor) {
        int word = navreqDescriptor[1];
        return ((word & 1) != 0) ? true : false;
    }

    private static int getCoarsenessFromNrd(int[] navreqDescriptor) {
        int word = navreqDescriptor[0];
        if (word % 2 == 0) {
            return 2;
        }
        else {
            return 3;
        }
    }

    private static int setCoarsenessInWord(int coarseness, int word) {
        if (coarseness == 2) {
            word &= ~1;
        }
        else {
            word |= 1;
        }
        return word;
    }

    private static int[] getNavreqDescriptor(int jobId) throws GameActionException {
        int wordAddr = Channels.NAVREQ_HEADER_TBL + (jobId * Channels.NAVREQ_HEADER_DESC_SZ);
        return Radio.getData(wordAddr, Channels.NAVREQ_HEADER_DESC_SZ);
    }

    private static void setNavreqDescriptor(int[] navreqDescriptor, int jobId)
            throws GameActionException {
        int wordAddr = Channels.NAVREQ_HEADER_TBL + (jobId * Channels.NAVREQ_HEADER_DESC_SZ);
        Radio.putData(wordAddr, navreqDescriptor);
    }

    private static int setRoundInWord(int round, int word) {
        int newWord = round << 3;
        if ((word & 1) != 0) {
            newWord |= 1;
        }
        if ((word & 2) != 0) {
            newWord |= 2;
        }
        if ((word & 4) != 0) {
            newWord |= 4;
        }
        return newWord;
    }

    private static int setFinishedInWord(boolean status, int word) {
        if (status) {
            word |= (1 << 2);
        }
        else {
            word &= ~(1 << 2);
        }
        return word;
    }

    private static int setComputingInWord(boolean status, int word) {
        if (status) {
            word |= (1 << 1);
        }
        else {
            word &= ~(1 << 1);
        }
        return word;
    }

    private static int setRequestedInWord(boolean status, int word) {
        if (status) {
            word |= 1;
        }
        else {
            word &= ~1;
        }
        return word;
    }

    private static boolean isComputingFromNrd(int[] navreqDescriptor) {
        int word = navreqDescriptor[1];
        if ((word & (1 << 1)) != 0) {
            return true;
        }
        else {
            return false;
        }
    }

    private static void printNavreqHeaderTable() throws GameActionException {
        int[] table = Radio.getData(Channels.NAVREQ_HEADER_TBL, 20);
        System.out.printf("========== NAVREQ DESCRIPTOR TABLE ==========\n");
        System.out.printf("==========         START           ==========\n");
        for (int i = 0; i < table.length; i++) {
            System.out.printf("%d:\t%08x\n", i, table[i]);
        }
        System.out.printf("==========          END            ==========\n");
    }
}
