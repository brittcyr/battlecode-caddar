package team050;

import team050.rpc.Channels;
import team050.rpc.Clans;
import team050.rpc.Clans.ClanMode;
import team050.rpc.Liveness;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class PastrRobot extends BaseRobot {

    public int gid  = -1;
    public int clan = -1;

    public PastrRobot(RobotController myRC) throws GameActionException {
        super(myRC);

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
        // TODO: Check if there are any requests for computation
    }

    protected void updateInternals() {
        // TODO: Potential distress signal
        // This is where we should decide if we call for reinforcements

        // Possible things to calculate
        // HQ
        // EnemyHQ
        // ME
        // Other PASTR
        // ENEMY PASTR1
        // ENEMY PASTR2
        // ENEMY PASTR3
        // ...
    }

    public void doAction() throws GameActionException {
        // GeneralNavigation.prepareCompute(rc, rc.getLocation());
        // GeneralNavigation.doCompute();
    }

    protected void sendUpdates() throws GameActionException {
        Liveness.updateLiveness(RobotType.PASTR, gid);
    }

    protected void doCompute() {
        // pass
        // The action of pastr is just computing
    }

}