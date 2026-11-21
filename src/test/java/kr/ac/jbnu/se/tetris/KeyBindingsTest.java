package kr.ac.jbnu.se.tetris;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.KeyEvent;
import org.junit.jupiter.api.Test;

class KeyBindingsTest {

    @Test
    void copyIsIndependent() {
        KeySettings a = KeySettings.defaults();
        KeySettings b = a.copy();
        assertTrue(a.equals(b));
        b.set(KeySettings.SINGLE, 0, KeyEvent.VK_X);
        assertFalse(a.equals(b));
        assertEquals(KeyEvent.VK_LEFT, a.get(KeySettings.SINGLE, 0));
    }

    @Test
    void conflictInSameTab() {
        KeySettings k = KeySettings.defaults();
        assertEquals("1인 모드 · 오른쪽 이동", k.findConflict(KeySettings.SINGLE, 0, KeyEvent.VK_RIGHT));
    }

    @Test
    void ownCurrentKeyIsNotAConflict() {
        KeySettings k = KeySettings.defaults();
        assertNull(k.findConflict(KeySettings.SINGLE, 0, KeyEvent.VK_LEFT));
    }

    @Test
    void singleTabDoesNotConflictWithP1() {
        KeySettings k = KeySettings.defaults();
        assertNull(k.findConflict(KeySettings.SINGLE, 0, KeyEvent.VK_A));
    }

    @Test
    void p1AndP2ConflictWithEachOther() {
        KeySettings k = KeySettings.defaults();
        assertEquals("2인 P2 · 왼쪽 이동", k.findConflict(KeySettings.P1, 0, KeyEvent.VK_LEFT));
    }

    @Test
    void pauseKeyConflictsWithEveryTab() {
        KeySettings k = KeySettings.defaults();
        assertEquals("공통 · 일시정지", k.findConflict(KeySettings.SINGLE, 0, KeyEvent.VK_P));
        assertEquals("1인 모드 · 왼쪽 이동", k.findConflict(KeySettings.COMMON, 0, KeyEvent.VK_LEFT));
    }

    @Test
    void ignoresModifierKeys() {
        assertTrue(KeySettings.isIgnoredKey(KeyEvent.VK_SHIFT));
        assertFalse(KeySettings.isIgnoredKey(KeyEvent.VK_A));
    }

    @Test
    void rejectsUnknownTab() {
        assertThrows(IllegalArgumentException.class, () -> KeySettings.defaultKeys(9));
    }
}
