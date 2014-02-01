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

    protected void getUpdates() {
        if (!isComputing) {
            jobID = CoopNav.claimNextAvailableJob();
            int jobCoarseness = CoopNav.getJobCoarseness(jobID);
            MapLocation jobTarget = CoopNav.getJobTarget(jobID);

            GeneralNavigation.coarseness = jobCoarseness;
            GeneralNavigation.prepareCompute(rc, jobTarget);
            isComputing = true;
        }
    }

    protected void updateInternals() {

    }

    public void doAction() throws GameActionException {
        if (isComputing) {
            GeneralNavigation.doCompute();
        }
    }

    protected void sendUpdates() throws GameActionException {
        Liveness.updateLiveness(RobotType.PASTR, gid);
        if (Dijkstra.finished) {

            isComputing = false;
            Dijkstra.finished = false;
            CoopNav.postJobResult(jobID, Dijkstra.previous);

            // Redundant because posting could take a really long time
            Liveness.updateLiveness(RobotType.PASTR, gid);
        }
    }

    protected void doCompute() {
        // pass
    }

}