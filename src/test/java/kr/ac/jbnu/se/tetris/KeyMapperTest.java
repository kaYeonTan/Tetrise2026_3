package kr.ac.jbnu.se.tetris;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.KeyEvent;

import org.junit.jupiter.api.Test;

import kr.ac.jbnu.se.tetris.core.PlayerAction;

class KeyMapperTest {
    private final int[] keys = {KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_S, KeyEvent.VK_SPACE,
        KeyEvent.VK_W, KeyEvent.VK_Q, KeyEvent.VK_E};

    @Test
    void mapsEachSlotToItsAction() {
        assertEquals(PlayerAction.MOVE_LEFT, KeyMapper.map(KeyEvent.VK_A, keys).get());
        assertEquals(PlayerAction.HARD_DROP, KeyMapper.map(KeyEvent.VK_SPACE, keys).get());
        assertEquals(PlayerAction.HOLD, KeyMapper.map(KeyEvent.VK_E, keys).get());
    }

    @Test
    void unknownKeyIsEmpty() {
        assertTrue(KeyMapper.map(KeyEvent.VK_Z, keys).isEmpty());
    }
}
