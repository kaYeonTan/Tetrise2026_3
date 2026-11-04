package kr.ac.jbnu.se.tetris;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GameModeStrategyTest {

    @Test
    void selectsModeFromFlags() {
        assertSame(GameModeStrategy.SINGLE, GameModeStrategy.of(false, false));
        assertSame(GameModeStrategy.LOCAL_COOP, GameModeStrategy.of(false, true));
        assertSame(GameModeStrategy.ONLINE_COOP, GameModeStrategy.of(true, true));
    }

    @Test
    void localCoopControlsBothPlayersAndOnlineOnlyLocalOne() {
        assertTrue(GameModeStrategy.LOCAL_COOP.controlsPlayer(0, 0));
        assertTrue(GameModeStrategy.LOCAL_COOP.controlsPlayer(1, 0));
        assertTrue(GameModeStrategy.ONLINE_COOP.controlsPlayer(1, 1));
        assertFalse(GameModeStrategy.ONLINE_COOP.controlsPlayer(0, 1));
    }

    @Test
    void onlineDoesNotRunLocalGravityAndPicksKeys() {
        assertFalse(GameModeStrategy.ONLINE_COOP.runsLocalGravity());
        assertTrue(GameModeStrategy.LOCAL_COOP.runsLocalGravity());
        int[] single = {1};
        int[] p1 = {2};
        assertArrayEquals(p1, GameModeStrategy.LOCAL_COOP.primaryKeys(single, p1));
        assertArrayEquals(single, GameModeStrategy.ONLINE_COOP.primaryKeys(single, p1));
    }
}
