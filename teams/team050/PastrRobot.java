package team050;

import team050.rpc.Channels;
import team050.rpc.Clans;
import team050.rpc.Clans.ClanMode;
import team050.rpc.CoopNav;
import team050.rpc.Liveness;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class PastrRobot extends BaseRobot {

    public int     gid         = -1;
    public int     clan        = -1;
    public boolean isComputing = false;
    public int     jobID       = -1;

    public PastrRobot(RobotController myRC) throws GameActionException {
        super(myRC);
        // GeneralNavigation.prepareCompute(rc, rc.getLocation());

        // Determine what clan supposed to be in, assign self gid.
        // TODO 10 should be constant
        // TODO We assume only one builder clan here. Maybe use clan private memory to fix.
        for (int i = 0; i < Channels.MAX_ROBOTS; i += 10) {
            int pclan = i / 10;
            ClanMode mode = Clans.getClanMode(pclan);
            if (mode == ClanMode.DEFENDER) {
                clan = pclan;
                for (int pgidOffset = 0; pgidOffset < 10; pgidOffset++) {
                    int pgid = i + pgidOffset;
                    if (Liveness.getLastPostedRoundByGid(pgid) == 0) {
                        gid = pgid;
                        Liveness.updateLiveness(RobotType.PASTR, gid);
                        Clans.setClanSize(clan, Clans.getClanSize(clan) + 1);
                        break;
                    }
                }
            }
        }
        assert (gid != -1);
        assert (clan != -1);
    }

    protected void getUpdates() throws GameActionException {
        // Check if we are free to take a new computing job
        if (!isComputing) {
            jobID = CoopNav.claimNextAvailableJob();

            // Check whether there is computation that needs to be done
            if (jobID == -1) {
                return;
            }

            // Get the parameters for the job
            int jobCoarseness = CoopNav.getJobCoarseness(jobID);
            MapLocation jobTarget = CoopNav.getJobTarget(jobID);

            // Tell GeneralNavigation what the next job should be
            GeneralNavigation.coarseness = jobCoarseness;
            GeneralNavigation.prepareCompute(rc, jobTarget);

            // Set the computing flag
            isComputing = true;
        }
    }

    protected void updateInternals() {
        // TODO: check for enemy pastrs and post them to the queue
    }

    public void doAction() throws GameActionException {
        // If we have a job prepared, then do computation
        if (isComputing) {
            GeneralNavigation.doCompute();
        }
    }

    protected void sendUpdates() throws GameActionException {
        // Update the liveness so that the hq knows we are alive
        Liveness.updateLiveness(RobotType.PASTR, gid);

        // This is a flag that tells us if the computation has finished
        if (Dijkstra.finished) {
            // We are no longer computing once this is done
            isComputing = false;

            // We are now finished, do not want this to accidentally run again
            Dijkstra.finished = false;

            // Tell the cowboys all the wonderful computing that we have done
            CoopNav.postJobResult(rc, jobID, Dijkstra.previous);

            // Redundant because posting could take a really long time
            Liveness.updateLiveness(RobotType.PASTR, gid);
        }
    }

    protected void doCompute() {
        // pass
    }

}