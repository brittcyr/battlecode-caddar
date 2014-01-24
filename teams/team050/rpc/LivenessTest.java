package team050.rpc;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import battlecode.common.GameConstants;
import battlecode.common.RobotType;

public class LivenessTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public final void testRoundAndTypeToIntHP() {
        for (int round = 0; round <= GameConstants.ROUND_MAX_LIMIT; round++) {
            for (RobotType type : RobotType.values()) {
                int word = Liveness.roundAndTypeToInt(round, type);
                assertEquals(round, Liveness.wordToRound(word));
                assertEquals(type, Liveness.wordToType(word));
            }
        }
    }
}
